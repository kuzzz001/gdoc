package com.gdoc.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("gdoc_comment")
public class GdocComment extends BaseEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long docId;

    private Long userId;

    private String content;

    private Integer rangeStart;

    private Integer rangeEnd;

    private Integer resolved;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRangeStart() { return rangeStart; }
    public void setRangeStart(Integer rangeStart) { this.rangeStart = rangeStart; }

    public Integer getRangeEnd() { return rangeEnd; }
    public void setRangeEnd(Integer rangeEnd) { this.rangeEnd = rangeEnd; }

    public Integer getResolved() { return resolved; }
    public void setResolved(Integer resolved) { this.resolved = resolved; }
}