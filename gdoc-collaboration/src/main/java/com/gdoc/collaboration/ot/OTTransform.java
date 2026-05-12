package com.gdoc.collaboration.ot;

import java.util.List;

public class OTTransform {

    public record TransformResult(OperationBuffer leftPrime, OperationBuffer rightPrime) {}

    public static TransformResult transform(OperationBuffer left, OperationBuffer right) {
        OperationBuffer leftPrime = new OperationBuffer();
        OperationBuffer rightPrime = new OperationBuffer();

        List<Operation> leftOps = left.getOperations();
        List<Operation> rightOps = right.getOperations();

        int leftIdx = 0, rightIdx = 0;
        Operation leftOp = leftIdx < leftOps.size() ? leftOps.get(leftIdx) : null;
        Operation rightOp = rightIdx < rightOps.size() ? rightOps.get(rightIdx) : null;

        while (leftOp != null || rightOp != null) {
            if (leftOp != null && leftOp.isInsert()) {
                leftPrime.add(Operation.insert(leftOp.getText()));
                leftOp = nextOp(leftOps, ++leftIdx);
                continue;
            }

            if (rightOp != null && rightOp.isInsert()) {
                rightPrime.add(Operation.insert(rightOp.getText()));
                rightOp = nextOp(rightOps, ++rightIdx);
                continue;
            }

            if (leftOp == null || rightOp == null) {
                break;
            }

            int leftCount = leftOp.getCount();
            int rightCount = rightOp.getCount();

            if (leftOp.isDelete() && rightOp.isRetain()) {
                leftPrime.add(Operation.delete(leftCount));
                leftOp = nextOp(leftOps, ++leftIdx);
            } else if (leftOp.isRetain() && rightOp.isDelete()) {
                rightPrime.add(Operation.delete(rightCount));
                rightOp = nextOp(rightOps, ++rightIdx);
            } else if (leftOp.isRetain() && rightOp.isRetain()) {
                int minCount = Math.min(leftCount, rightCount);
                leftPrime.add(Operation.retain(minCount));
                rightPrime.add(Operation.retain(minCount));
                leftOp = consumeCount(leftOp, minCount);
                rightOp = consumeCount(rightOp, minCount);
                leftIdx = advance(leftOps, leftOp, leftIdx);
                rightIdx = advance(rightOps, rightOp, rightIdx);
                leftOp = leftIdx < leftOps.size() ? leftOps.get(leftIdx) : null;
                rightOp = rightIdx < rightOps.size() ? rightOps.get(rightIdx) : null;
            } else if (leftOp.isDelete() && rightOp.isDelete()) {
                int minCount = Math.min(leftCount, rightCount);
                leftOp = consumeCount(leftOp, minCount);
                rightOp = consumeCount(rightOp, minCount);
                leftIdx = advance(leftOps, leftOp, leftIdx);
                rightIdx = advance(rightOps, rightOp, rightIdx);
                leftOp = leftIdx < leftOps.size() ? leftOps.get(leftIdx) : null;
                rightOp = rightIdx < rightOps.size() ? rightOps.get(rightIdx) : null;
            } else {
                break;
            }
        }

        return new TransformResult(leftPrime, rightPrime);
    }

    private static Operation nextOp(List<Operation> ops, int idx) {
        return idx < ops.size() ? ops.get(idx) : null;
    }

    private static Operation consumeCount(Operation op, int count) {
        if (op == null) return null;
        int newCount = op.getCount() - count;
        if (newCount <= 0) return null;
        Operation newOp = op.clone();
        newOp.setCount(newCount);
        return newOp;
    }

    private static int advance(List<Operation> ops, Operation op, int idx) {
        if (op == null || op.getCount() <= 0) {
            return idx;
        }
        return idx - 1;
    }
}
