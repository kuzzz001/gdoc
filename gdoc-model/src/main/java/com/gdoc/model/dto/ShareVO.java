package com.gdoc.model.dto;

import java.time.LocalDateTime;

public class ShareVO {

    private Long id;
    private Long docId;
    private String token;
    private String permission;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private String shareUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }
}