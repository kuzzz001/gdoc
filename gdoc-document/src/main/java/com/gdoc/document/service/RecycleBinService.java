package com.gdoc.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdoc.document.mapper.DocumentMapper;
import com.gdoc.model.dto.DocumentVO;
import com.gdoc.model.entity.GdocDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecycleBinService {

    private final DocumentMapper documentMapper;

    public RecycleBinService(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    public IPage<DocumentVO> listDeleted(Long userId, int pageNum, int pageSize) {
        Page<GdocDocument> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GdocDocument> wrapper = new LambdaQueryWrapper<GdocDocument>()
                .eq(GdocDocument::getOwnerId, userId)
                .eq(GdocDocument::getDeleted, 1)
                .orderByDesc(GdocDocument::getUpdatedAt);

        IPage<GdocDocument> docPage = documentMapper.selectPage(page, wrapper);
        return docPage.convert(doc -> {
            DocumentVO vo = new DocumentVO();
            vo.setId(doc.getId());
            vo.setTitle(doc.getTitle());
            vo.setOwnerId(doc.getOwnerId());
            vo.setVersion(doc.getVersion());
            vo.setCreatedAt(doc.getCreatedAt());
            vo.setUpdatedAt(doc.getUpdatedAt());
            vo.setPermission("owner");
            return vo;
        });
    }

    @Transactional
    public void restore(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || !doc.getOwnerId().equals(userId)) {
            throw new com.gdoc.common.exception.BusinessException(
                    com.gdoc.common.result.ResultCode.DOC_NOT_FOUND);
        }
        doc.setDeleted(0);
        documentMapper.updateById(doc);
    }

    @Transactional
    public void permanentDelete(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || !doc.getOwnerId().equals(userId)) {
            throw new com.gdoc.common.exception.BusinessException(
                    com.gdoc.common.result.ResultCode.DOC_NOT_FOUND);
        }
        documentMapper.deleteById(docId);
    }

    @Transactional
    public void emptyBin(Long userId) {
        LambdaQueryWrapper<GdocDocument> wrapper = new LambdaQueryWrapper<GdocDocument>()
                .eq(GdocDocument::getOwnerId, userId)
                .eq(GdocDocument::getDeleted, 1);
        documentMapper.delete(wrapper);
    }
}