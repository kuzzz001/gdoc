package com.gdoc.collaboration.ot;

public class OTEngine {

    public static String apply(String doc, OperationBuffer ops) {
        if (doc == null) doc = "";
        TextBuffer buffer = new TextBuffer(doc);

        int pos = 0;
        for (Operation op : ops.getOperations()) {
            switch (op.getType()) {
                case RETAIN -> pos += op.getCount();
                case INSERT -> {
                    buffer.insert(pos, op.getText());
                    pos += op.getText().length();
                }
                case DELETE -> buffer.delete(pos, pos + op.getCount());
            }
        }
        return buffer.getText();
    }

    public static OperationBuffer compose(OperationBuffer ops1, OperationBuffer ops2) {
        if (ops1.isEmpty()) return ops2.clone();
        if (ops2.isEmpty()) return ops1.clone();

        OperationBuffer result = new OperationBuffer();
        java.util.List<Operation> list1 = ops1.getOperations();
        java.util.List<Operation> list2 = ops2.getOperations();

        int i1 = 0, i2 = 0;
        Operation op1 = i1 < list1.size() ? list1.get(i1) : null;
        Operation op2 = i2 < list2.size() ? list2.get(i2) : null;

        while (op1 != null || op2 != null) {
            if (op1 != null && op1.isInsert()) {
                result.add(op1.clone());
                op1 = nextOp(list1, ++i1);
                continue;
            }

            if (op2 != null && op2.isDelete()) {
                result.add(op2.clone());
                op2 = nextOp(list2, ++i2);
                continue;
            }

            if (op1 == null || op2 == null) {
                break;
            }

            if (op1.isDelete() && op2.isRetain()) {
                result.add(op1.clone());
                op1 = nextOp(list1, ++i1);
            } else if (op1.isRetain() && op2.isInsert()) {
                result.add(op2.clone());
                op2 = nextOp(list2, ++i2);
            } else if (op1.isRetain() && op2.isRetain()) {
                int min = Math.min(op1.getCount(), op2.getCount());
                result.add(Operation.retain(min));
                op1 = consume(op1, min);
                op2 = consume(op2, min);
                i1 = advance(list1, op1, i1);
                i2 = advance(list2, op2, i2);
                op1 = i1 < list1.size() ? list1.get(i1) : null;
                op2 = i2 < list2.size() ? list2.get(i2) : null;
            } else if (op1.isDelete() && op2.isDelete()) {
                int min = Math.min(op1.getCount(), op2.getCount());
                op1 = consume(op1, min);
                op2 = consume(op2, min);
                i1 = advance(list1, op1, i1);
                i2 = advance(list2, op2, i2);
                op1 = i1 < list1.size() ? list1.get(i1) : null;
                op2 = i2 < list2.size() ? list2.get(i2) : null;
            } else {
                break;
            }
        }

        return result;
    }

    public static OperationBuffer invert(OperationBuffer ops, String doc) {
        OperationBuffer inverted = new OperationBuffer();
        TextBuffer buffer = new TextBuffer(doc);
        int pos = 0;

        for (Operation op : ops.getOperations()) {
            switch (op.getType()) {
                case RETAIN -> pos += op.getCount();
                case INSERT -> {
                    inverted.add(Operation.delete(op.getText().length()));
                    pos += op.getText().length();
                }
                case DELETE -> {
                    String deleted = buffer.slice(pos, pos + op.getCount());
                    inverted.add(Operation.insert(deleted));
                }
            }
        }
        return inverted;
    }

    private static Operation nextOp(java.util.List<Operation> list, int idx) {
        return idx < list.size() ? list.get(idx) : null;
    }

    private static Operation consume(Operation op, int count) {
        if (op == null) return null;
        int newCount = op.getCount() - count;
        if (newCount <= 0) return null;
        Operation newOp = op.clone();
        newOp.setCount(newCount);
        return newOp;
    }

    private static int advance(java.util.List<Operation> list, Operation op, int idx) {
        if (op == null || op.getCount() > 0) return idx;
        return idx - 1;
    }
}
