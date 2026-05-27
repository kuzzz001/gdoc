package com.gdoc.model.dto;

import java.util.List;

public class FolderVO {
    private Long id;
    private String name;
    private Long parentId;
    private List<FolderVO> children;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public List<FolderVO> getChildren() { return children; }
    public void setChildren(List<FolderVO> children) { this.children = children; }
}