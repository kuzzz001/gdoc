package com.gdoc.model.dto;

import jakarta.validation.constraints.Pattern;

public class CreateShareRequest {

    @Pattern(regexp = "view|editor", message = "权限必须是 view 或 editor")
    private String permission = "view";

    private Integer expireHours;

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public Integer getExpireHours() { return expireHours; }
    public void setExpireHours(Integer expireHours) { this.expireHours = expireHours; }
}