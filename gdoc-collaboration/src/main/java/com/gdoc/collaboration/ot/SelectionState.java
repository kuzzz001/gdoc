package com.gdoc.collaboration.ot;

import java.util.HashMap;
import java.util.Map;

public class SelectionState {
    private Long userId;
    private String username;
    private String color;
    private int anchor;
    private int head;

    public SelectionState() {}

    public SelectionState(Long userId, String username, String color, int anchor, int head) {
        this.userId = userId;
        this.username = username;
        this.color = color;
        this.anchor = anchor;
        this.head = head;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("username", username);
        map.put("color", color);
        map.put("anchor", anchor);
        map.put("head", head);
        return map;
    }

    public static SelectionState fromJson(Map<String, Object> json) {
        return new SelectionState(
            json.get("userId") != null ? ((Number) json.get("userId")).longValue() : null,
            (String) json.get("username"),
            (String) json.get("color"),
            json.get("anchor") != null ? ((Number) json.get("anchor")).intValue() : 0,
            json.get("head") != null ? ((Number) json.get("head")).intValue() : 0
        );
    }

    public SelectionState transform(Delta delta, boolean priority) {
        int newAnchor = transformPosition(anchor, delta, priority);
        int newHead = transformPosition(head, delta, priority);
        return new SelectionState(userId, username, color, newAnchor, newHead);
    }

    private int transformPosition(int pos, Delta delta, boolean priority) {
        int newPos = pos;
        for (Delta.DeltaOp op : delta.getOps()) {
            if (op.getType() == Delta.DeltaOpType.INSERT) {
                int len = op.getText() != null ? op.getText().length() : 0;
                if (newPos > op.getCount() || (newPos == op.getCount() && priority)) {
                    newPos += len;
                }
            } else if (op.getType() == Delta.DeltaOpType.DELETE) {
                newPos -= Math.min(op.getCount(), newPos);
            }
        }
        return Math.max(0, newPos);
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getAnchor() { return anchor; }
    public void setAnchor(int anchor) { this.anchor = anchor; }

    public int getHead() { return head; }
    public void setHead(int head) { this.head = head; }
}