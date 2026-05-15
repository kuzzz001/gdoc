package com.gdoc.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.document.mapper.CollaboratorMapper;
import com.gdoc.document.mapper.DocumentMapper;
import com.gdoc.document.mapper.ShareMapper;
import com.gdoc.model.dto.*;
import com.gdoc.model.entity.GdocCollaborator;
import com.gdoc.model.entity.GdocDocument;
import com.gdoc.model.entity.GdocShare;
import com.gdoc.model.entity.GdocUser;
import com.gdoc.security.annotation.DocPermission;
import com.gdoc.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentMapper documentMapper;
    private final ShareMapper shareMapper;
    private final CollaboratorMapper collaboratorMapper;
    private final UserMapper userMapper;
    private final PermissionService permissionService;

    public DocumentService(DocumentMapper documentMapper,
                           ShareMapper shareMapper,
                           CollaboratorMapper collaboratorMapper,
                           UserMapper userMapper,
                           PermissionService permissionService) {
        this.documentMapper = documentMapper;
        this.shareMapper = shareMapper;
        this.collaboratorMapper = collaboratorMapper;
        this.userMapper = userMapper;
        this.permissionService = permissionService;
    }

    @Transactional
    public DocumentVO create(DocumentCreateRequest request, Long ownerId) {
        GdocDocument doc = new GdocDocument();
        doc.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "无标题文档");
        doc.setContent(request.getContent());
        doc.setOwnerId(ownerId);
        doc.setVersion(1);
        documentMapper.insert(doc);

        return toVO(doc, ownerId);
    }

    public IPage<DocumentVO> list(Long userId, int pageNum, int pageSize) {
        Page<GdocDocument> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<GdocDocument> wrapper = new LambdaQueryWrapper<GdocDocument>()
                .eq(GdocDocument::getOwnerId, userId)
                .or()
                .exists("SELECT 1 FROM gdoc_collaborator c WHERE c.doc_id = gdoc_document.id AND c.user_id = {0}", userId)
                .orderByDesc(GdocDocument::getUpdatedAt);

        IPage<GdocDocument> docPage = documentMapper.selectPage(page, wrapper);

        return docPage.convert(doc -> toVO(doc, userId));
    }

    public DocumentVO getById(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        checkPermission(doc, userId);
        return toVO(doc, userId);
    }

    @Transactional
    public DocumentVO update(Long docId, DocumentUpdateRequest request, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        DocPermission perm = permissionService.getPermission(docId, userId);
        if (perm == null || (perm != DocPermission.OWNER && perm != DocPermission.EDITOR)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        doc.setTitle(request.getTitle());
        if (request.getContent() != null) {
            doc.setContent(request.getContent());
        }
        doc.setVersion(doc.getVersion() + 1);
        documentMapper.updateById(doc);
        return toVO(doc, userId);
    }

    @Transactional
    public void delete(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        documentMapper.deleteById(docId);
    }

    @Transactional
    public ShareVO createShare(Long docId, String permission, Integer expireHours, Long userId) {
        CreateShareRequest request = new CreateShareRequest();
        request.setPermission(permission);
        request.setExpireHours(expireHours);
        return createShare(docId, request, userId);
    }

    @Transactional
    public ShareVO createShare(Long docId, CreateShareRequest request, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        String permission = request.getPermission() != null ? request.getPermission() : "view";
        String token = UUID.randomUUID().toString().replace("-", "");

        GdocShare share = new GdocShare();
        share.setDocId(docId);
        share.setToken(token);
        share.setPermission(permission);
        if (request.getExpireHours() != null && request.getExpireHours() > 0) {
            share.setExpireAt(LocalDateTime.now().plusHours(request.getExpireHours()));
        }
        shareMapper.insert(share);
        return toShareVO(share);
    }


    public DocumentVO getByShareToken(String token, Long userId) {
        GdocShare share = shareMapper.selectOne(new LambdaQueryWrapper<GdocShare>()
                .eq(GdocShare::getToken, token));

        if (share == null) {
            throw new BusinessException(ResultCode.DOC_SHARE_NOT_FOUND);
        }

        if (share.getExpireAt() != null && share.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.DOC_SHARE_EXPIRED);
        }

        GdocDocument doc = documentMapper.selectById(share.getDocId());
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }

        DocumentVO vo = toVO(doc, userId);
        vo.setPermission(share.getPermission());
        return vo;
    }

    public List<ShareVO> listShares(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        List<GdocShare> shares = shareMapper.selectList(new LambdaQueryWrapper<GdocShare>()
                .eq(GdocShare::getDocId, docId)
                .orderByDesc(GdocShare::getCreatedAt));

        return shares.stream().map(this::toShareVO).collect(Collectors.toList());
    }

    @Transactional
    public void revokeShare(Long docId, String token, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        GdocShare share = shareMapper.selectOne(new LambdaQueryWrapper<GdocShare>()
                .eq(GdocShare::getDocId, docId)
                .eq(GdocShare::getToken, token));
        if (share == null) {
            throw new BusinessException(ResultCode.DOC_SHARE_NOT_FOUND);
        }

        shareMapper.deleteById(share.getId());
    }

    public List<CollaboratorVO> listCollaborators(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        checkPermission(doc, userId);

        List<GdocCollaborator> collaborators = collaboratorMapper.selectList(
                new LambdaQueryWrapper<GdocCollaborator>()
                        .eq(GdocCollaborator::getDocId, docId)
                        .orderByAsc(GdocCollaborator::getCreatedAt));

        return collaborators.stream().map(c -> {
            CollaboratorVO vo = new CollaboratorVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setRole(c.getRole());
            vo.setCreatedAt(c.getCreatedAt());

            GdocUser user = userMapper.selectById(c.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public CollaboratorVO addCollaborator(Long docId, AddCollaboratorRequest request, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        GdocUser targetUser = userMapper.selectOne(new LambdaQueryWrapper<GdocUser>()
                .eq(GdocUser::getUsername, request.getUsername()));
        if (targetUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (targetUser.getId().equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能将自己添加为协作者");
        }

        if (targetUser.getId().equals(doc.getOwnerId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该用户是文档拥有者，无需添加为协作者");
        }

        boolean exists = collaboratorMapper.exists(new LambdaQueryWrapper<GdocCollaborator>()
                .eq(GdocCollaborator::getDocId, docId)
                .eq(GdocCollaborator::getUserId, targetUser.getId()));
        if (exists) {
            throw new BusinessException(ResultCode.DOC_COLLABORATOR_EXISTS);
        }

        GdocCollaborator collaborator = new GdocCollaborator();
        collaborator.setDocId(docId);
        collaborator.setUserId(targetUser.getId());
        collaborator.setRole(request.getRole());
        collaboratorMapper.insert(collaborator);

        CollaboratorVO vo = new CollaboratorVO();
        vo.setId(collaborator.getId());
        vo.setUserId(targetUser.getId());
        vo.setUsername(targetUser.getUsername());
        vo.setNickname(targetUser.getNickname());
        vo.setRole(collaborator.getRole());
        vo.setCreatedAt(collaborator.getCreatedAt());
        return vo;
    }

    @Transactional
    public void removeCollaborator(Long docId, Long targetUserId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }
        if (doc.getOwnerId().equals(targetUserId)) {
            throw new BusinessException(ResultCode.DOC_CANNOT_REMOVE_OWNER);
        }

        GdocCollaborator collaborator = collaboratorMapper.selectOne(new LambdaQueryWrapper<GdocCollaborator>()
                .eq(GdocCollaborator::getDocId, docId)
                .eq(GdocCollaborator::getUserId, targetUserId));
        if (collaborator == null) {
            throw new BusinessException(ResultCode.DOC_COLLABORATOR_NOT_FOUND);
        }

        collaboratorMapper.deleteById(collaborator.getId());
    }

    @Transactional
    public CollaboratorVO updateCollaboratorRole(Long docId, Long targetUserId,
                                                  UpdateCollaboratorRoleRequest request, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        GdocCollaborator collaborator = collaboratorMapper.selectOne(new LambdaQueryWrapper<GdocCollaborator>()
                .eq(GdocCollaborator::getDocId, docId)
                .eq(GdocCollaborator::getUserId, targetUserId));
        if (collaborator == null) {
            throw new BusinessException(ResultCode.DOC_COLLABORATOR_NOT_FOUND);
        }

        collaborator.setRole(request.getRole());
        collaboratorMapper.updateById(collaborator);

        CollaboratorVO vo = new CollaboratorVO();
        vo.setId(collaborator.getId());
        vo.setUserId(collaborator.getUserId());
        vo.setRole(collaborator.getRole());
        vo.setCreatedAt(collaborator.getCreatedAt());

        GdocUser user = userMapper.selectById(collaborator.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }
        return vo;
    }

    public String addCollaborator(Long docId, Long targetUserId, String role, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        boolean exists = collaboratorMapper.exists(new LambdaQueryWrapper<GdocCollaborator>()
                .eq(GdocCollaborator::getDocId, docId)
                .eq(GdocCollaborator::getUserId, targetUserId));
        if (exists) {
            return "协作者已存在";
        }

        GdocCollaborator collaborator = new GdocCollaborator();
        collaborator.setDocId(docId);
        collaborator.setUserId(targetUserId);
        collaborator.setRole(role);
        collaboratorMapper.insert(collaborator);
        return "协作者已添加";
    }

    private void checkPermission(GdocDocument doc, Long userId) {
        if (doc.getOwnerId().equals(userId)) {
            return;
        }
        boolean isCollaborator = collaboratorMapper.exists(new LambdaQueryWrapper<GdocCollaborator>()
                .eq(GdocCollaborator::getDocId, doc.getId())
                .eq(GdocCollaborator::getUserId, userId));
        if (!isCollaborator) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }
    }

    private DocumentVO toVO(GdocDocument doc, Long currentUserId) {
        DocumentVO vo = new DocumentVO();
        vo.setId(doc.getId());
        vo.setTitle(doc.getTitle());
        vo.setContent(doc.getContent());
        vo.setOwnerId(doc.getOwnerId());
        vo.setVersion(doc.getVersion());
        vo.setCreatedAt(doc.getCreatedAt());
        vo.setUpdatedAt(doc.getUpdatedAt());

        if (currentUserId != null && doc.getOwnerId().equals(currentUserId)) {
            vo.setPermission("owner");
        } else if (currentUserId != null) {
            GdocCollaborator collab = collaboratorMapper.selectOne(new LambdaQueryWrapper<GdocCollaborator>()
                    .eq(GdocCollaborator::getDocId, doc.getId())
                    .eq(GdocCollaborator::getUserId, currentUserId));
            vo.setPermission(collab != null ? collab.getRole() : null);
        }

        GdocUser owner = userMapper.selectById(doc.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(owner.getNickname() != null ? owner.getNickname() : owner.getUsername());
        }

        return vo;
    }

    private ShareVO toShareVO(GdocShare share) {
        ShareVO vo = new ShareVO();
        vo.setId(share.getId());
        vo.setDocId(share.getDocId());
        vo.setToken(share.getToken());
        vo.setPermission(share.getPermission());
        vo.setExpireAt(share.getExpireAt());
        vo.setCreatedAt(share.getCreatedAt());
        return vo;
    }

    public List<SnapshotVO> listSnapshots(Long docId, int page, int size) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        return java.util.Collections.emptyList();
    }

    public String getSnapshotContent(Long docId, int version) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        return doc.getContent();
    }

    @Transactional
    public void rollbackToVersion(Long docId, int version, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }
    }
}
