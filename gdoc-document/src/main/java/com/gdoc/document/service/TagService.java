package com.gdoc.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.document.mapper.DocumentTagMapper;
import com.gdoc.document.mapper.TagMapper;
import com.gdoc.model.dto.DocumentTagRequest;
import com.gdoc.model.dto.TagCreateRequest;
import com.gdoc.model.dto.TagVO;
import com.gdoc.model.entity.GdocDocumentTag;
import com.gdoc.model.entity.GdocTag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagMapper tagMapper;
    private final DocumentTagMapper documentTagMapper;

    public TagService(TagMapper tagMapper, DocumentTagMapper documentTagMapper) {
        this.tagMapper = tagMapper;
        this.documentTagMapper = documentTagMapper;
    }

    public TagVO create(Long ownerId, TagCreateRequest request) {
        // Check duplicate
        boolean exists = tagMapper.exists(new LambdaQueryWrapper<GdocTag>()
                .eq(GdocTag::getOwnerId, ownerId)
                .eq(GdocTag::getName, request.getName()));
        if (exists) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "标签已存在");
        }

        GdocTag tag = new GdocTag();
        tag.setName(request.getName());
        tag.setOwnerId(ownerId);
        tagMapper.insert(tag);

        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setDocIds(new ArrayList<>());
        return vo;
    }

    public List<TagVO> list(Long ownerId) {
        List<GdocTag> tags = tagMapper.selectList(new LambdaQueryWrapper<GdocTag>()
                .eq(GdocTag::getOwnerId, ownerId)
                .orderByAsc(GdocTag::getName));

        return tags.stream().map(tag -> {
            TagVO vo = new TagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());

            List<GdocDocumentTag> docTags = documentTagMapper.selectList(
                    new LambdaQueryWrapper<GdocDocumentTag>().eq(GdocDocumentTag::getTagId, tag.getId()));
            vo.setDocIds(docTags.stream().map(GdocDocumentTag::getDocId).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long tagId, Long ownerId) {
        GdocTag tag = tagMapper.selectById(tagId);
        if (tag == null || !tag.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "标签不存在");
        }
        documentTagMapper.delete(new LambdaQueryWrapper<GdocDocumentTag>()
                .eq(GdocDocumentTag::getTagId, tagId));
        tagMapper.deleteById(tagId);
    }

    @Transactional
    public void tagDocument(Long docId, Long ownerId, DocumentTagRequest request) {
        // Remove existing tags
        documentTagMapper.delete(new LambdaQueryWrapper<GdocDocumentTag>()
                .eq(GdocDocumentTag::getDocId, docId));

        // Add new tags (verify ownership)
        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                GdocTag tag = tagMapper.selectById(tagId);
                if (tag != null && tag.getOwnerId().equals(ownerId)) {
                    GdocDocumentTag docTag = new GdocDocumentTag();
                    docTag.setDocId(docId);
                    docTag.setTagId(tagId);
                    documentTagMapper.insert(docTag);
                }
            }
        }
    }

    public List<TagVO> getDocumentTags(Long docId) {
        List<GdocDocumentTag> docTags = documentTagMapper.selectList(
                new LambdaQueryWrapper<GdocDocumentTag>().eq(GdocDocumentTag::getDocId, docId));

        return docTags.stream().map(dt -> {
            GdocTag tag = tagMapper.selectById(dt.getTagId());
            if (tag != null) {
                TagVO vo = new TagVO();
                vo.setId(tag.getId());
                vo.setName(tag.getName());
                return vo;
            }
            return null;
        }).filter(t -> t != null).collect(Collectors.toList());
    }
}