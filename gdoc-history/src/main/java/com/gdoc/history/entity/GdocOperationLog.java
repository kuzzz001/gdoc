package com.gdoc.history.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

@TableName("gdoc_operation_log")
public class GdocOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long docId;

    private Long userId;

    private String username;

    private Integer operationType;

    private String operations;

    private Integer version;

    private String contentSnapshot;

    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    public static final int TYPE_INSERT = 1;
    public static final int TYPE_DELETE = 2;
    public static final int TYPE_RETAIN = 3;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getOperationType() { return operationType; }
    public void setOperationType(Integer operationType) { this.operationType = operationType; }

    public String getOperations() { return operations; }
    public void setOperations(String operations) { this.operations = operations; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getContentSnapshot() { return contentSnapshot; }
    public void setContentSnapshot(String contentSnapshot) { this.contentSnapshot = contentSnapshot; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}