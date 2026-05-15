package com.gdoc.collaboration.ot;

import java.util.ArrayList;
import java.util.List;

public class OperationBuffer {
    private final List<Operation> operations = new ArrayList<>();

    public void add(Operation op) {
        if (op == null || op.getCount() == 0) return;

        if (!operations.isEmpty()) {
            Operation last = operations.get(operations.size() - 1);
            if (last.getType() == op.getType()) {
                if (op.isRetain() || (op.isInsert() && op.getText().equals(last.getText()))) {
                    last.setCount(last.getCount() + op.getCount());
                    return;
                }
                if (op.isInsert() && last.isInsert()) {
                    last.setCount(last.getCount() + op.getText().length());
                    return;
                }
            }
        }
        operations.add(op);
    }

    public void addAll(List<Operation> ops) {
        for (Operation op : ops) {
            add(op);
        }
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public OperationBuffer clone() {
        OperationBuffer buffer = new OperationBuffer();
        for (Operation op : operations) {
            buffer.add(op.clone());
        }
        return buffer;
    }

    public boolean isEmpty() {
        return operations.isEmpty();
    }

    public int size() {
        return operations.size();
    }

    public int getLength() {
        int length = 0;
        for (Operation op : operations) {
            if (op.isRetain() || op.isInsert()) {
                length += op.getCount();
            } else if (op.isDelete()) {
                length -= op.getCount();
            }
        }
        return length;
    }
}