package com.gdoc.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.document.mapper.CollaboratorMapper;
import com.gdoc.document.mapper.DocumentMapper;
import com.gdoc.model.entity.GdocCollaborator;
import com.gdoc.model.entity.GdocDocument;
import com.gdoc.security.annotation.DocPermission;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final DocumentMapper documentMapper;
    private final CollaboratorMapper collaboratorMapper;

    public PermissionService(DocumentMapper documentMapper, CollaboratorMapper collaboratorMapper) {
        this.documentMapper = documentMapper;
        this.collaboratorMapper = collaboratorMapper;
    }

    public DocPermission getPermission(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null) {
            return null;
        }
        if (doc.getOwnerId().equals(userId)) {
            return DocPermission.OWNER;
        }
        GdocCollaborator collab = collaboratorMapper.selectOne(new LambdaQueryWrapper<GdocCollaborator>()
                .eq(GdocCollaborator::getDocId, docId)
                .eq(GdocCollaborator::getUserId, userId));
        if (collab == null) {
            return null;
        }
        if ("editor".equals(collab.getRole())) {
            return DocPermission.EDITOR;
        }
        if ("viewer".equals(collab.getRole())) {
            return DocPermission.VIEWER;
        }
        return null;
    }
}