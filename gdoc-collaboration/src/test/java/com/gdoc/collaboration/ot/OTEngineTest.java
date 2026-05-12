package com.gdoc.collaboration.ot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OTEngineTest {

    @Test
    void testApplyInsert() {
        OperationBuffer ops = new OperationBuffer();
        ops.add(Operation.insert("hello"));

        String result = OTEngine.apply("world", ops);
        assertEquals("helloworld", result);
    }

    @Test
    void testApplyDelete() {
        OperationBuffer ops = new OperationBuffer();
        ops.add(Operation.delete(5));

        String result = OTEngine.apply("helloworld", ops);
        assertEquals("world", result);
    }

    @Test
    void testApplyRetain() {
        OperationBuffer ops = new OperationBuffer();
        ops.add(Operation.retain(5));

        String result = OTEngine.apply("hello", ops);
        assertEquals("hello", result);
    }

    @Test
    void testApplyInsertAndDelete() {
        OperationBuffer ops = new OperationBuffer();
        ops.add(Operation.insert("XX"));
        ops.add(Operation.delete(5));

        String result = OTEngine.apply("helloworld", ops);
        assertEquals("XXworld", result);
    }

    @Test
    void testTransformInsertInsert() {
        OperationBuffer left = new OperationBuffer();
        left.add(Operation.insert("A"));

        OperationBuffer right = new OperationBuffer();
        right.add(Operation.insert("B"));

        OTTransform.TransformResult result = OTTransform.transform(left, right);

        assertEquals("A", opsToString(result.leftPrime()));
        assertEquals("B", opsToString(result.rightPrime()));
    }

    @Test
    void testTransformInsertRetain() {
        OperationBuffer left = new OperationBuffer();
        left.add(Operation.insert("ABC"));

        OperationBuffer right = new OperationBuffer();
        right.add(Operation.retain(3));

        OTTransform.TransformResult tr = OTTransform.transform(left, right);

        assertEquals("ABC", opsToString(tr.leftPrime()));
        assertEquals(3, tr.rightPrime().getLength());
    }

    @Test
    void testTransformRetainDelete() {
        OperationBuffer left = new OperationBuffer();
        left.add(Operation.retain(5));

        OperationBuffer right = new OperationBuffer();
        right.add(Operation.delete(3));
        right.add(Operation.retain(2));

        OTTransform.TransformResult tr = OTTransform.transform(left, right);

        assertEquals(5, tr.leftPrime().getLength());
        assertEquals("delete(3)+retain(2)", opsToString(tr.rightPrime()));
    }

    @Test
    void testTransformDeleteInsert() {
        OperationBuffer left = new OperationBuffer();
        left.add(Operation.delete(3));

        OperationBuffer right = new OperationBuffer();
        right.add(Operation.insert("X"));

        OTTransform.TransformResult tr = OTTransform.transform(left, right);

        assertEquals("delete(3)", opsToString(tr.leftPrime()));
        assertEquals("X", opsToString(tr.rightPrime()));
    }

    @Test
    void testComposeEmpty() {
        OperationBuffer ops1 = new OperationBuffer();
        OperationBuffer ops2 = new OperationBuffer();

        OperationBuffer result = OTEngine.compose(ops1, ops2);
        assertTrue(result.isEmpty());
    }

    @Test
    void testComposeInsertInsert() {
        OperationBuffer ops1 = new OperationBuffer();
        ops1.add(Operation.insert("AB"));

        OperationBuffer ops2 = new OperationBuffer();
        ops2.add(Operation.retain(2));
        ops2.add(Operation.insert("CD"));

        OperationBuffer result = OTEngine.compose(ops1, ops2);
        assertEquals("ABCD", opsToString(result));
    }

    @Test
    void testComposeRetainInsert() {
        OperationBuffer ops1 = new OperationBuffer();
        ops1.add(Operation.retain(3));

        OperationBuffer ops2 = new OperationBuffer();
        ops2.add(Operation.retain(3));
        ops2.add(Operation.insert("X"));

        OperationBuffer result = OTEngine.compose(ops1, ops2);
        assertEquals("X", opsToString(result));
    }

    @Test
    void testInvertInsert() {
        OperationBuffer ops = new OperationBuffer();
        ops.add(Operation.insert("hello"));

        OperationBuffer inverted = OTEngine.invert(ops, "");
        assertEquals("delete(5)", opsToString(inverted));
    }

    @Test
    void testInvertDelete() {
        OperationBuffer ops = new OperationBuffer();
        ops.add(Operation.delete(5));

        OperationBuffer inverted = OTEngine.invert(ops, "hello");
        assertEquals("hello", opsToString(inverted));
    }

    @Test
    void testInvertRetain() {
        OperationBuffer ops = new OperationBuffer();
        ops.add(Operation.retain(5));

        OperationBuffer inverted = OTEngine.invert(ops, "hello");
        assertTrue(inverted.isEmpty());
    }

    @Test
    void testOperationBufferClone() {
        OperationBuffer original = new OperationBuffer();
        original.add(Operation.insert("test"));

        OperationBuffer cloned = original.clone();
        assertEquals(original.getLength(), cloned.getLength());
        assertNotSame(original, cloned);
    }

    @Test
    void testOperationBufferMerge() {
        OperationBuffer buffer = new OperationBuffer();
        buffer.add(Operation.insert("hello"));
        buffer.add(Operation.insert("hello"));

        assertEquals("hello", opsToString(buffer));
        assertEquals(1, buffer.getOperations().size());
    }

    @Test
    void testOperationBufferNoMergeDifferentText() {
        OperationBuffer buffer = new OperationBuffer();
        buffer.add(Operation.insert("hello"));
        buffer.add(Operation.insert("world"));

        assertEquals("hello+world", opsToString(buffer));
        assertEquals(2, buffer.getOperations().size());
    }

    private String opsToString(OperationBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        for (Operation op : buffer.getOperations()) {
            if (sb.length() > 0) sb.append("+");
            switch (op.getType()) {
                case INSERT -> sb.append(op.getText());
                case DELETE -> sb.append("delete(").append(op.getCount()).append(")");
                case RETAIN -> sb.append("retain(").append(op.getCount()).append(")");
            }
        }
        return sb.toString();
    }
}
