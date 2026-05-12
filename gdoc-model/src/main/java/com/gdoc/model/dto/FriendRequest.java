package com.gdoc.model.dto;

import jakarta.validation.constraints.NotBlank;

public class FriendRequest {

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
