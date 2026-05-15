package com.gdoc.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DocumentUpdateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 256, message = "标题最长256个字符")
    private String title;

    private String content;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}