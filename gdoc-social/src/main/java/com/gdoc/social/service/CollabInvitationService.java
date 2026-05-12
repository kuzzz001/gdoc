package com.gdoc.social.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.model.dto.CollabInvitationVO;
import com.gdoc.model.dto.CollabInvitationRequest;
import com.gdoc.model.entity.GdocCollabInvitation;
import com.gdoc.model.entity.GdocCollaborator;
import com.gdoc.model.entity.GdocDocument;
import com.gdoc.model.entity.GdocUser;
import com.gdoc.document.mapper.CollaboratorMapper;
import com.gdoc.document.mapper.DocumentMapper;
import com.gdoc.social.mapper.CollabInvitationMapper;
import com.gdoc.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CollabInvitationService {

    private final CollabInvitationMapper invitationMapper;
    private final UserMapper userMapper;
    private final DocumentMapper documentMapper;
    private final CollaboratorMapper collaboratorMapper;
    private final FriendshipService friendshipService;

    public CollabInvitationService(CollabInvitationMapper invitationMapper,
                                   UserMapper userMapper,
                                   DocumentMapper documentMapper,
                                   CollaboratorMapper collaboratorMapper,
                                   FriendshipService friendshipService) {
        this.invitationMapper = invitationMapper;
        this.userMapper = userMapper;
        this.documentMapper = documentMapper;
        this.collaboratorMapper = collaboratorMapper;
        this.friendshipService = friendshipService;
    }

    @Transactional
    public CollabInvitationVO sendInvitation(Long inviterId, CollabInvitationRequest request) {
        if (!friendshipService.isFriend(inviterId, request.getInviteeId())) {
            throw new BusinessException(ResultCode.INVITATION_NOT_FRIEND);
        }

        GdocDocument doc = documentMapper.selectById(request.getDocId());
        if (doc == null) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        if (!doc.getOwnerId().equals(inviterId)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        boolean alreadyCollaborator = collaboratorMapper.exists(new LambdaQueryWrapper<GdocCollaborator>()
                .eq(GdocCollaborator::getDocId, request.getDocId())
                .eq(GdocCollaborator::getUserId, request.getInviteeId()));
        if (alreadyCollaborator) {
            throw new BusinessException(ResultCode.DOC_COLLABORATOR_EXISTS);
        }

        boolean existingInvitation = invitationMapper.exists(new LambdaQueryWrapper<GdocCollabInvitation>()
                .eq(GdocCollabInvitation::getDocId, request.getDocId())
                .eq(GdocCollabInvitation::getInviteeId, request.getInviteeId())
                .eq(GdocCollabInvitation::getStatus, GdocCollabInvitation.STATUS_PENDING));
        if (existingInvitation) {
            throw new BusinessException(ResultCode.INVITATION_ALREADY_HANDLED);
        }

        GdocCollabInvitation invitation = new GdocCollabInvitation();
        invitation.setDocId(request.getDocId());
        invitation.setInviterId(inviterId);
        invitation.setInviteeId(request.getInviteeId());
        invitation.setRole(request.getRole() != null ? request.getRole() : "editor");
        invitation.setStatus(GdocCollabInvitation.STATUS_PENDING);
        invitation.setMessage(request.getMessage());
        invitationMapper.insert(invitation);

        return toVO(invitation);
    }

    @Transactional
    public void acceptInvitation(Long userId, Long invitationId) {
        GdocCollabInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new BusinessException(ResultCode.INVITATION_NOT_FOUND);
        }
        if (!invitation.getInviteeId().equals(userId)) {
            throw new BusinessException(ResultCode.INVITATION_NOT_FOUND);
        }
        if (invitation.getStatus() != GdocCollabInvitation.STATUS_PENDING) {
            throw new BusinessException(ResultCode.INVITATION_ALREADY_HANDLED);
        }

        invitation.setStatus(GdocCollabInvitation.STATUS_ACCEPTED);
        invitationMapper.updateById(invitation);

        GdocCollaborator collaborator = new GdocCollaborator();
        collaborator.setDocId(invitation.getDocId());
        collaborator.setUserId(invitation.getInviteeId());
        collaborator.setRole(invitation.getRole());
        collaboratorMapper.insert(collaborator);
    }

    public void rejectInvitation(Long userId, Long invitationId) {
        GdocCollabInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new BusinessException(ResultCode.INVITATION_NOT_FOUND);
        }
        if (!invitation.getInviteeId().equals(userId)) {
            throw new BusinessException(ResultCode.INVITATION_NOT_FOUND);
        }
        if (invitation.getStatus() != GdocCollabInvitation.STATUS_PENDING) {
            throw new BusinessException(ResultCode.INVITATION_ALREADY_HANDLED);
        }

        invitation.setStatus(GdocCollabInvitation.STATUS_REJECTED);
        invitationMapper.updateById(invitation);
    }

    public void cancelInvitation(Long inviterId, Long invitationId) {
        GdocCollabInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new BusinessException(ResultCode.INVITATION_NOT_FOUND);
        }
        if (!invitation.getInviterId().equals(inviterId)) {
            throw new BusinessException(ResultCode.INVITATION_NOT_FOUND);
        }
        if (invitation.getStatus() != GdocCollabInvitation.STATUS_PENDING) {
            throw new BusinessException(ResultCode.INVITATION_ALREADY_HANDLED);
        }

        invitation.setStatus(GdocCollabInvitation.STATUS_CANCELLED);
        invitationMapper.updateById(invitation);
    }

    public List<CollabInvitationVO> getReceivedInvitations(Long userId) {
        List<GdocCollabInvitation> invitations = invitationMapper.selectList(new LambdaQueryWrapper<GdocCollabInvitation>()
                .eq(GdocCollabInvitation::getInviteeId, userId)
                .orderByDesc(GdocCollabInvitation::getCreatedAt));
        return invitations.stream().map(this::toVO).toList();
    }

    public List<CollabInvitationVO> getSentInvitations(Long userId) {
        List<GdocCollabInvitation> invitations = invitationMapper.selectList(new LambdaQueryWrapper<GdocCollabInvitation>()
                .eq(GdocCollabInvitation::getInviterId, userId)
                .orderByDesc(GdocCollabInvitation::getCreatedAt));
        return invitations.stream().map(this::toVO).toList();
    }

    private CollabInvitationVO toVO(GdocCollabInvitation invitation) {
        CollabInvitationVO vo = new CollabInvitationVO();
        vo.setId(invitation.getId());
        vo.setDocId(invitation.getDocId());
        vo.setInviterId(invitation.getInviterId());
        vo.setInviteeId(invitation.getInviteeId());
        vo.setRole(invitation.getRole());
        vo.setStatus(invitation.getStatus());
        vo.setMessage(invitation.getMessage());
        vo.setCreatedAt(invitation.getCreatedAt());

        GdocDocument doc = documentMapper.selectById(invitation.getDocId());
        if (doc != null) {
            vo.setDocTitle(doc.getTitle());
        }

        GdocUser inviter = userMapper.selectById(invitation.getInviterId());
        if (inviter != null) {
            vo.setInviterName(inviter.getNickname() != null ? inviter.getNickname() : inviter.getUsername());
            vo.setInviterAvatar(inviter.getAvatarUrl());
        }

        GdocUser invitee = userMapper.selectById(invitation.getInviteeId());
        if (invitee != null) {
            vo.setInviteeName(invitee.getNickname() != null ? invitee.getNickname() : invitee.getUsername());
        }

        return vo;
    }
}
