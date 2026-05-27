package com.gdoc.model.dto;

import java.time.LocalDateTime;

public class CommentVO {
    private Long id;
    private Long docId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String content;
    private Integer rangeStart;
    private Integer rangeEnd;
    private Integer resolved;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRangeStart() { return rangeStart; }
    public void setRangeStart(Integer rangeStart) { this.rangeStart = rangeStart; }

    public Integer getRangeEnd() { return rangeEnd; }
    public void setRangeEnd(Integer rangeEnd) { this.rangeEnd = rangeEnd; }

    public Integer getResolved() { return resolved; }
    public void setResolved(Integer resolved) { this.resolved = resolved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}