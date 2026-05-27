package com.gdoc.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.document.mapper.DocumentVersionMapper;
import com.gdoc.model.entity.GdocDocumentVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VersionService {

    private final DocumentVersionMapper versionMapper;

    public VersionService(DocumentVersionMapper versionMapper) {
        this.versionMapper = versionMapper;
    }

    @Transactional
    public GdocDocumentVersion createVersion(Long docId, String content, String name, Long userId) {
        Long count = versionMapper.selectCount(
                new LambdaQueryWrapper<GdocDocumentVersion>().eq(GdocDocumentVersion::getDocId, docId));

        GdocDocumentVersion version = new GdocDocumentVersion();
        version.setDocId(docId);
        version.setContent(content);
        version.setVersionNumber(count.intValue() + 1);
        version.setVersionName(name != null ? name : "v" + (count.intValue() + 1));
        version.setCreatedBy(userId);
        versionMapper.insert(version);
        return version;
    }

    public List<GdocDocumentVersion> listVersions(Long docId) {
        return versionMapper.selectList(
                new LambdaQueryWrapper<GdocDocumentVersion>()
                        .eq(GdocDocumentVersion::getDocId, docId)
                        .orderByDesc(GdocDocumentVersion::getVersionNumber));
    }

    public GdocDocumentVersion getVersion(Long docId, Integer versionNumber) {
        return versionMapper.selectOne(
                new LambdaQueryWrapper<GdocDocumentVersion>()
                        .eq(GdocDocumentVersion::getDocId, docId)
                        .eq(GdocDocumentVersion::getVersionNumber, versionNumber));
    }

    @Transactional
    public void renameVersion(Long versionId, String newName) {
        GdocDocumentVersion version = versionMapper.selectById(versionId);
        if (version != null) {
            version.setVersionName(newName);
            versionMapper.updateById(version);
        }
    }
}