package com.gdoc.model.dto;

import java.util.List;

public class TagVO {

    private Long id;
    private String name;
    private List<Long> docIds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Long> getDocIds() { return docIds; }
    public void setDocIds(List<Long> docIds) { this.docIds = docIds; }
}