package com.gdoc.document.service;

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
        // Use raw SQL to bypass @TableLogic auto-filter which adds deleted=0
        IPage<GdocDocument> docPage = documentMapper.selectDeletedDocs(page, userId);
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
        GdocDocument doc = documentMapper.selectDeletedById(docId);
        if (doc == null || !doc.getOwnerId().equals(userId)) {
            throw new com.gdoc.common.exception.BusinessException(
                    com.gdoc.common.result.ResultCode.DOC_NOT_FOUND);
        }
        // Use raw SQL to bypass @TableLogic which would block update on deleted=1 rows
        documentMapper.restoreDeletedDoc(docId);
    }

    @Transactional
    public void permanentDelete(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectDeletedById(docId);
        if (doc == null || !doc.getOwnerId().equals(userId)) {
            throw new com.gdoc.common.exception.BusinessException(
                    com.gdoc.common.result.ResultCode.DOC_NOT_FOUND);
        }
        // Use raw SQL to bypass @TableLogic which blocks delete on already-deleted rows
        documentMapper.physicalDeleteDoc(docId);
    }

    @Transactional
    public void emptyBin(Long userId) {
        // Use raw SQL to bypass @TableLogic
        documentMapper.physicalDeleteAllDeleted(userId);
    }
}