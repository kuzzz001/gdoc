package com.gdoc.social.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.model.dto.CollabInvitationRequest;
import com.gdoc.model.dto.CollabInvitationVO;
import com.gdoc.social.service.CollabInvitationService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social/invitations")
public class CollabInvitationController {

    private final CollabInvitationService invitationService;

    public CollabInvitationController(CollabInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/send")
    public ApiResponse<CollabInvitationVO> sendInvitation(@Valid @RequestBody CollabInvitationRequest request,
                                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CollabInvitationVO invitation = invitationService.sendInvitation(userId, request);
        return ApiResponse.success("协作邀请已发送", invitation);
    }

    @PostMapping("/accept/{invitationId}")
    public ApiResponse<Void> acceptInvitation(@PathVariable Long invitationId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        invitationService.acceptInvitation(userId, invitationId);
        return ApiResponse.success("已接受协作邀请", null);
    }

    @PostMapping("/reject/{invitationId}")
    public ApiResponse<Void> rejectInvitation(@PathVariable Long invitationId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        invitationService.rejectInvitation(userId, invitationId);
        return ApiResponse.success("已拒绝协作邀请", null);
    }

    @PostMapping("/cancel/{invitationId}")
    public ApiResponse<Void> cancelInvitation(@PathVariable Long invitationId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        invitationService.cancelInvitation(userId, invitationId);
        return ApiResponse.success("已取消协作邀请", null);
    }

    @GetMapping("/received")
    public ApiResponse<List<CollabInvitationVO>> getReceivedInvitations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<CollabInvitationVO> invitations = invitationService.getReceivedInvitations(userId);
        return ApiResponse.success(invitations);
    }

    @GetMapping("/sent")
    public ApiResponse<List<CollabInvitationVO>> getSentInvitations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<CollabInvitationVO> invitations = invitationService.getSentInvitations(userId);
        return ApiResponse.success(invitations);
    }
}
