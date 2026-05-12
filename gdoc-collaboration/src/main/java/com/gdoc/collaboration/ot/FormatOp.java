package com.gdoc.collaboration.ot;

import java.util.HashMap;
import java.util.Map;

public class FormatOp {
    private OpType type;
    private int count;
    private String text;
    private Map<String, Object> attributes;

    public static FormatOp retain(int n) {
        FormatOp op = new FormatOp();
        op.type = OpType.RETAIN;
        op.count = n;
        op.attributes = new HashMap<>();
        return op;
    }

    public static FormatOp insert(String text) {
        FormatOp op = new FormatOp();
        op.type = OpType.INSERT;
        op.text = text;
        op.count = text != null ? text.length() : 0;
        op.attributes = new HashMap<>();
        return op;
    }

    public static FormatOp insert(String text, Map<String, Object> attrs) {
        FormatOp op = insert(text);
        if (attrs != null) {
            op.attributes.putAll(attrs);
        }
        return op;
    }

    public static FormatOp delete(int n) {
        FormatOp op = new FormatOp();
        op.type = OpType.DELETE;
        op.count = n;
        op.attributes = new HashMap<>();
        return op;
    }

    public FormatOp withAttribute(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
        return this;
    }

    public boolean isRetain() { return type == OpType.RETAIN; }
    public boolean isInsert() { return type == OpType.INSERT; }
    public boolean isDelete() { return type == OpType.DELETE; }

    public int getLength() { return count; }

    public FormatOp clone() {
        FormatOp cloned = new FormatOp();
        cloned.type = this.type;
        cloned.count = this.count;
        cloned.text = this.text;
        cloned.attributes = this.attributes != null ? new HashMap<>(this.attributes) : new HashMap<>();
        return cloned;
    }

    @Override
    public String toString() {
        return switch (type) {
            case RETAIN -> "retain(" + count + ")";
            case INSERT -> "insert(\"" + text + "\"" + (attributes.isEmpty() ? "" : ", " + attributes) + ")";
            case DELETE -> "delete(" + count + ")";
        };
    }

    public OpType getType() { return type; }
    public void setType(OpType type) { this.type = type; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}