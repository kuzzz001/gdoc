package com.gdoc.model.dto;

import jakarta.validation.constraints.NotBlank;

public class TemplateCreateRequest {
    @NotBlank(message = "模板名称不能为空")
    private String name;
    private String description;
    private String content;
    private String category;
    private Integer isPublic = 0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }
}