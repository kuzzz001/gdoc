package com.gdoc.model.dto;

import jakarta.validation.constraints.NotBlank;

public class FolderCreateRequest {
    @NotBlank
    private String name;
    private Long parentId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}