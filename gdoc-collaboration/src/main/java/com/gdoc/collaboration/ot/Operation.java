package com.gdoc.collaboration.ot;

public class Operation {
    private OpType type;
    private int count;
    private String text;

    public static Operation retain(int n) {
        Operation op = new Operation();
        op.type = OpType.RETAIN;
        op.count = n;
        return op;
    }

    public static Operation insert(String text) {
        Operation op = new Operation();
        op.type = OpType.INSERT;
        op.text = text;
        op.count = text != null ? text.length() : 0;
        return op;
    }

    public static Operation delete(int n) {
        Operation op = new Operation();
        op.type = OpType.DELETE;
        op.count = n;
        return op;
    }

    public boolean isRetain() { return type == OpType.RETAIN; }
    public boolean isInsert() { return type == OpType.INSERT; }
    public boolean isDelete() { return type == OpType.DELETE; }

    public int getLength() { return count; }

    public Operation clone() {
        Operation op = new Operation();
        op.type = this.type;
        op.count = this.count;
        op.text = this.text;
        return op;
    }

    @Override
    public String toString() {
        return switch (type) {
            case RETAIN -> "retain(" + count + ")";
            case INSERT -> "insert(\"" + text + "\")";
            case DELETE -> "delete(" + count + ")";
        };
    }

    public OpType getType() { return type; }
    public void setType(OpType type) { this.type = type; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}