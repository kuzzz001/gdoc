package com.gdoc.model.dto;

import jakarta.validation.constraints.NotNull;

public class CollabInvitationRequest {

    @NotNull(message = "文档ID不能为空")
    private Long docId;

    @NotNull(message = "被邀请者ID不能为空")
    private Long inviteeId;

    private String role = "editor";
    private String message;

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public Long getInviteeId() { return inviteeId; }
    public void setInviteeId(Long inviteeId) { this.inviteeId = inviteeId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
