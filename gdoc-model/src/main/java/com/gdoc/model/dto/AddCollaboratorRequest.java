package com.gdoc.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AddCollaboratorRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "editor|viewer", message = "角色必须是 editor 或 viewer")
    private String role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}