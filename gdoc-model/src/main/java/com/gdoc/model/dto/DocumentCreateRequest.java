package com.gdoc.model.dto;

import jakarta.validation.constraints.Size;

public class DocumentCreateRequest {

    @Size(max = 256, message = "标题最长256个字符")
    private String title;

    @Size(max = 65535, message = "内容过长")
    private String content;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}