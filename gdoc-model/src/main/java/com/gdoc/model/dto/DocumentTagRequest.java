package com.gdoc.model.dto;

import java.util.List;

public class DocumentTagRequest {

    private List<Long> tagIds;

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
}