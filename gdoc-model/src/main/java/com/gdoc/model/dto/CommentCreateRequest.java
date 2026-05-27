package com.gdoc.model.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentCreateRequest {
    @NotBlank
    private String content;
    private Integer rangeStart;
    private Integer rangeEnd;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRangeStart() { return rangeStart; }
    public void setRangeStart(Integer rangeStart) { this.rangeStart = rangeStart; }

    public Integer getRangeEnd() { return rangeEnd; }
    public void setRangeEnd(Integer rangeEnd) { this.rangeEnd = rangeEnd; }
}