package com.gdoc.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.document.mapper.TemplateMapper;
import com.gdoc.model.dto.TemplateCreateRequest;
import com.gdoc.model.dto.TemplateVO;
import com.gdoc.model.entity.GdocTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private final TemplateMapper templateMapper;

    public TemplateService(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    @Transactional
    public TemplateVO create(Long ownerId, TemplateCreateRequest request) {
        GdocTemplate template = new GdocTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setContent(request.getContent());
        template.setCategory(request.getCategory());
        template.setOwnerId(ownerId);
        template.setIsPublic(request.getIsPublic());
        templateMapper.insert(template);
        return toVO(template);
    }

    public List<TemplateVO> list(Long userId) {
        List<GdocTemplate> templates = templateMapper.selectList(
                new LambdaQueryWrapper<GdocTemplate>()
                        .eq(GdocTemplate::getOwnerId, userId)
                        .or()
                        .eq(GdocTemplate::getIsPublic, 1)
                        .orderByDesc(GdocTemplate::getCreatedAt));
        return templates.stream().map(this::toVO).collect(Collectors.toList());
    }

    public TemplateVO getById(Long id) {
        GdocTemplate template = templateMapper.selectById(id);
        return template != null ? toVO(template) : null;
    }

    @Transactional
    public void delete(Long id, Long userId) {
        GdocTemplate template = templateMapper.selectById(id);
        if (template != null && template.getOwnerId().equals(userId)) {
            templateMapper.deleteById(id);
        }
    }

    private TemplateVO toVO(GdocTemplate t) {
        TemplateVO vo = new TemplateVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setDescription(t.getDescription());
        vo.setContent(t.getContent());
        vo.setCategory(t.getCategory());
        vo.setOwnerId(t.getOwnerId());
        vo.setIsPublic(t.getIsPublic());
        vo.setCreatedAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
        return vo;
    }
}