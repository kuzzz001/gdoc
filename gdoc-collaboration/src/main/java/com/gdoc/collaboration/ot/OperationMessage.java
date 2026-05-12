package com.gdoc.collaboration.ot;

import java.util.List;

public class OperationMessage {
    private String type;
    private Long docId;
    private Long userId;
    private String username;
    private Long version;
    private List<Operation> operations;
    private Long baseVersion;
    private Long cursorPosition;
    private Long cursorEnd;

    public static final String OP_INSERT = "op_insert";
    public static final String OP_DELETE = "op_delete";
    public static final String OP_REPLACE = "op_replace";
    public static final String FULL_SYNC = "full_sync";
    public static final String USER_JOINED = "user_joined";
    public static final String USER_LEFT = "user_left";
    public static final String CURSOR_UPDATE = "cursor_update";
    public static final String SYNC_ACK = "sync_ack";

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<Operation> getOperations() { return operations; }
    public void setOperations(List<Operation> operations) { this.operations = operations; }

    public Long getBaseVersion() { return baseVersion; }
    public void setBaseVersion(Long baseVersion) { this.baseVersion = baseVersion; }

    public Long getCursorPosition() { return cursorPosition; }
    public void setCursorPosition(Long cursorPosition) { this.cursorPosition = cursorPosition; }

    public Long getCursorEnd() { return cursorEnd; }
    public void setCursorEnd(Long cursorEnd) { this.cursorEnd = cursorEnd; }
}