package com.gdoc.collaboration.ot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Delta {
    private final List<DeltaOp> ops;

    public Delta() {
        this.ops = new ArrayList<>();
    }

    public Delta(List<DeltaOp> ops) {
        this.ops = ops != null ? ops : new ArrayList<>();
    }

    public Delta retain(int length) {
        return retain(length, null);
    }

    public Delta retain(int length, Map<String, Object> attributes) {
        if (length <= 0) return this;
        ops.add(new DeltaOp(DeltaOpType.RETAIN, length, null, attributes));
        return this;
    }

    public Delta insert(String text) {
        return insert(text, null);
    }

    public Delta insert(String text, Map<String, Object> attributes) {
        if (text == null || text.isEmpty()) return this;
        ops.add(new DeltaOp(DeltaOpType.INSERT, text.length(), text, attributes));
        return this;
    }

    public Delta delete(int length) {
        if (length <= 0) return this;
        ops.add(new DeltaOp(DeltaOpType.DELETE, length, null, null));
        return this;
    }

    public List<DeltaOp> getOps() {
        return ops;
    }

    public boolean isEmpty() {
        return ops.isEmpty();
    }

    public int getLength() {
        int len = 0;
        for (DeltaOp op : ops) {
            if (op.getType() == DeltaOpType.RETAIN || op.getType() == DeltaOpType.DELETE) {
                len += op.getCount();
            } else if (op.getType() == DeltaOpType.INSERT) {
                len += op.getText() != null ? op.getText().length() : 0;
            }
        }
        return len;
    }

    public Delta compose(Delta other) {
        Delta result = new Delta();
        List<DeltaOp> thisOps = this.ops;
        List<DeltaOp> otherOps = other.ops;
        int i = 0, j = 0;

        while (i < thisOps.size() || j < otherOps.size()) {
            DeltaOp thisOp = i < thisOps.size() ? thisOps.get(i) : null;
            DeltaOp otherOp = j < otherOps.size() ? otherOps.get(j) : null;

            if (otherOp != null && otherOp.getType() == DeltaOpType.INSERT) {
                result.insert(otherOp.getText(), otherOp.getAttributes());
                j++;
            } else if (thisOp != null && thisOp.getType() == DeltaOpType.DELETE) {
                result.delete(thisOp.getCount());
                i++;
            } else if (thisOp != null && otherOp != null && thisOp.getType() == DeltaOpType.RETAIN && otherOp.getType() == DeltaOpType.RETAIN) {
                int minCount = Math.min(thisOp.getCount(), otherOp.getCount());
                result.retain(minCount, mergeAttributes(thisOp.getAttributes(), otherOp.getAttributes()));
                if (thisOp.getCount() > minCount) {
                    thisOps.set(i, new DeltaOp(DeltaOpType.RETAIN, thisOp.getCount() - minCount, null, thisOp.getAttributes()));
                } else {
                    i++;
                }
                if (otherOp.getCount() > minCount) {
                    otherOps.set(j, new DeltaOp(DeltaOpType.RETAIN, otherOp.getCount() - minCount, null, otherOp.getAttributes()));
                } else {
                    j++;
                }
            } else if (thisOp != null && thisOp.getType() == DeltaOpType.RETAIN && otherOp != null && otherOp.getType() == DeltaOpType.DELETE) {
                int minCount = Math.min(thisOp.getCount(), otherOp.getCount());
                result.delete(minCount);
                if (thisOp.getCount() > minCount) {
                    thisOps.set(i, new DeltaOp(DeltaOpType.RETAIN, thisOp.getCount() - minCount, null, thisOp.getAttributes()));
                } else {
                    i++;
                }
                if (otherOp.getCount() > minCount) {
                    otherOps.set(j, new DeltaOp(DeltaOpType.DELETE, otherOp.getCount() - minCount, null, null));
                } else {
                    j++;
                }
            } else if (thisOp != null && otherOp != null && thisOp.getType() == DeltaOpType.INSERT && otherOp.getType() == DeltaOpType.RETAIN) {
                int minCount = Math.min(thisOp.getText().length(), otherOp.getCount());
                result.insert(thisOp.getText().substring(0, minCount), otherOp.getAttributes());
                if (thisOp.getText().length() > minCount) {
                    thisOps.set(i, new DeltaOp(DeltaOpType.INSERT, 0, thisOp.getText().substring(minCount), thisOp.getAttributes()));
                } else {
                    i++;
                }
                if (otherOp.getCount() > minCount) {
                    otherOps.set(j, new DeltaOp(DeltaOpType.RETAIN, otherOp.getCount() - minCount, null, otherOp.getAttributes()));
                } else {
                    j++;
                }
            } else if (thisOp != null && otherOp != null && thisOp.getType() == DeltaOpType.INSERT && otherOp.getType() == DeltaOpType.DELETE) {
                int minCount = Math.min(thisOp.getText().length(), otherOp.getCount());
                if (minCount > 0) {
                    thisOps.set(i, new DeltaOp(DeltaOpType.INSERT, 0, thisOp.getText().substring(minCount), thisOp.getAttributes()));
                    otherOps.set(j, new DeltaOp(DeltaOpType.DELETE, otherOp.getCount() - minCount, null, null));
                } else {
                    i++;
                    j++;
                }
            } else if (thisOp != null && thisOp.getType() == DeltaOpType.RETAIN && otherOp == null) {
                result.retain(thisOp.getCount(), thisOp.getAttributes());
                i++;
            } else if (otherOp != null && otherOp.getType() == DeltaOpType.RETAIN && thisOp == null) {
                result.retain(otherOp.getCount(), otherOp.getAttributes());
                j++;
            } else {
                if (thisOp != null) i++;
                if (otherOp != null) j++;
            }
        }

        return result;
    }

    public Delta transform(Delta other, boolean priority) {
        Delta result = new Delta();
        List<DeltaOp> thisOps = this.ops;
        List<DeltaOp> otherOps = other.ops;
        int i = 0, j = 0;

        while (i < thisOps.size() || j < otherOps.size()) {
            DeltaOp thisOp = i < thisOps.size() ? thisOps.get(i) : null;
            DeltaOp otherOp = j < otherOps.size() ? otherOps.get(j) : null;

            if (thisOp != null && thisOp.getType() == DeltaOpType.DELETE) {
                result.delete(thisOp.getCount());
                i++;
                continue;
            }

            if (otherOp != null && otherOp.getType() == DeltaOpType.INSERT) {
                result.insert(otherOp.getText(), otherOp.getAttributes());
                j++;
                continue;
            }

            if (thisOp == null || otherOp == null) {
                if (thisOp != null) {
                    result.retain(thisOp.getCount(), thisOp.getAttributes());
                    i++;
                }
                if (otherOp != null) {
                    j++;
                }
                continue;
            }

            if (thisOp.getType() == DeltaOpType.RETAIN && otherOp.getType() == DeltaOpType.RETAIN) {
                int minCount = Math.min(thisOp.getCount(), otherOp.getCount());
                result.retain(minCount);
                if (thisOp.getCount() > minCount) {
                    thisOps.set(i, new DeltaOp(DeltaOpType.RETAIN, thisOp.getCount() - minCount, null, thisOp.getAttributes()));
                } else {
                    i++;
                }
                if (otherOp.getCount() > minCount) {
                    otherOps.set(j, new DeltaOp(DeltaOpType.RETAIN, otherOp.getCount() - minCount, null, otherOp.getAttributes()));
                } else {
                    j++;
                }
            } else if (thisOp.getType() == DeltaOpType.INSERT && otherOp.getType() == DeltaOpType.DELETE) {
                int minCount = Math.min(thisOp.getText().length(), otherOp.getCount());
                if (thisOp.getText().length() > minCount) {
                    thisOps.set(i, new DeltaOp(DeltaOpType.INSERT, 0, thisOp.getText().substring(minCount), thisOp.getAttributes()));
                } else {
                    i++;
                }
                if (otherOp.getCount() > minCount) {
                    otherOps.set(j, new DeltaOp(DeltaOpType.DELETE, otherOp.getCount() - minCount, null, null));
                } else {
                    j++;
                }
            } else if (thisOp.getType() == DeltaOpType.INSERT && otherOp.getType() == DeltaOpType.RETAIN) {
                int minCount = Math.min(thisOp.getText().length(), otherOp.getCount());
                result.insert(thisOp.getText().substring(0, minCount), thisOp.getAttributes());
                if (thisOp.getText().length() > minCount) {
                    thisOps.set(i, new DeltaOp(DeltaOpType.INSERT, 0, thisOp.getText().substring(minCount), thisOp.getAttributes()));
                } else {
                    i++;
                }
                if (otherOp.getCount() > minCount) {
                    otherOps.set(j, new DeltaOp(DeltaOpType.RETAIN, otherOp.getCount() - minCount, null, otherOp.getAttributes()));
                } else {
                    j++;
                }
            } else if (thisOp.getType() == DeltaOpType.RETAIN && otherOp.getType() == DeltaOpType.DELETE) {
                int minCount = Math.min(thisOp.getCount(), otherOp.getCount());
                result.delete(minCount);
                if (thisOp.getCount() > minCount) {
                    thisOps.set(i, new DeltaOp(DeltaOpType.RETAIN, thisOp.getCount() - minCount, null, thisOp.getAttributes()));
                } else {
                    i++;
                }
                if (otherOp.getCount() > minCount) {
                    otherOps.set(j, new DeltaOp(DeltaOpType.DELETE, otherOp.getCount() - minCount, null, null));
                } else {
                    j++;
                }
            }
        }

        return priority ? result : new Delta();
    }

    private Map<String, Object> mergeAttributes(Map<String, Object> left, Map<String, Object> right) {
        if (left == null && right == null) return null;
        Map<String, Object> result = new HashMap<>();
        if (left != null) result.putAll(left);
        if (right != null) result.putAll(right);
        return result.isEmpty() ? null : result;
    }

    public enum DeltaOpType {
        INSERT, DELETE, RETAIN
    }

    public static class DeltaOp {
        private DeltaOpType type;
        private int count;
        private String text;
        private Map<String, Object> attributes;

        public DeltaOp() {}

        public DeltaOp(DeltaOpType type, int count, String text, Map<String, Object> attributes) {
            this.type = type;
            this.count = count;
            this.text = text;
            this.attributes = attributes;
        }

        public DeltaOpType getType() { return type; }
        public void setType(DeltaOpType type) { this.type = type; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public Map<String, Object> getAttributes() { return attributes; }
        public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    }

    public static Delta fromJson(Map<String, Object> json) {
        Delta delta = new Delta();
        List<Map<String, Object>> ops = (List<Map<String, Object>>) json.get("ops");
        if (ops != null) {
            for (Map<String, Object> op : ops) {
                String opType = (String) op.get("insert");
                if (opType != null) {
                    if (opType instanceof String) {
                        delta.insert(opType, (Map<String, Object>) op.get("attributes"));
                    } else {
                        delta.insert(String.valueOf(opType), (Map<String, Object>) op.get("attributes"));
                    }
                } else if (op.containsKey("retain")) {
                    int retainCount = ((Number) op.get("retain")).intValue();
                    delta.retain(retainCount, (Map<String, Object>) op.get("attributes"));
                } else if (op.containsKey("delete")) {
                    int deleteCount = ((Number) op.get("delete")).intValue();
                    delta.delete(deleteCount);
                }
            }
        }
        return delta;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        List<Map<String, Object>> opsList = new ArrayList<>();
        for (DeltaOp op : ops) {
            Map<String, Object> opMap = new HashMap<>();
            switch (op.getType()) {
                case INSERT -> {
                    opMap.put("insert", op.getText());
                    if (op.getAttributes() != null && !op.getAttributes().isEmpty()) {
                        opMap.put("attributes", op.getAttributes());
                    }
                }
                case DELETE -> opMap.put("delete", op.getCount());
                case RETAIN -> {
                    opMap.put("retain", op.getCount());
                    if (op.getAttributes() != null && !op.getAttributes().isEmpty()) {
                        opMap.put("attributes", op.getAttributes());
                    }
                }
            }
            opsList.add(opMap);
        }
        json.put("ops", opsList);
        return json;
    }
}
