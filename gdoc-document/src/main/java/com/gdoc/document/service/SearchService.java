package com.gdoc.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdoc.document.mapper.DocumentMapper;
import com.gdoc.model.dto.DocumentVO;
import com.gdoc.model.entity.GdocDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SearchService {

    private final DocumentMapper documentMapper;
    private final DocumentService documentService;

    public SearchService(DocumentMapper documentMapper, DocumentService documentService) {
        this.documentMapper = documentMapper;
        this.documentService = documentService;
    }

    public IPage<DocumentVO> search(Long userId, String keyword, int pageNum, int pageSize) {
        if (!StringUtils.hasText(keyword)) {
            return new Page<>(pageNum, pageSize);
        }

        Page<GdocDocument> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GdocDocument> wrapper = new LambdaQueryWrapper<GdocDocument>()
                .eq(GdocDocument::getOwnerId, userId)
                .and(w -> w.like(GdocDocument::getTitle, keyword)
                        .or()
                        .like(GdocDocument::getContent, keyword))
                .orderByDesc(GdocDocument::getUpdatedAt);

        IPage<GdocDocument> docPage = documentMapper.selectPage(page, wrapper);
        return docPage.convert(doc -> documentService.getById(doc.getId(), userId));
    }
}