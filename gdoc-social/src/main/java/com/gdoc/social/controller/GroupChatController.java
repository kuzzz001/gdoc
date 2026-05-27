package com.gdoc.social.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.social.service.GroupChatService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupChatController {

    private final GroupChatService groupChatService;

    public GroupChatController(GroupChatService groupChatService) {
        this.groupChatService = groupChatService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(groupChatService.create(userId, body.get("name")));
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(groupChatService.list(userId));
    }

    @PostMapping("/{groupId}/members")
    public ApiResponse<Void> addMember(@PathVariable Long groupId,
                                       @RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        groupChatService.addMember(groupId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ApiResponse<Void> removeMember(@PathVariable Long groupId,
                                          @PathVariable Long userId) {
        groupChatService.removeMember(groupId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{groupId}/messages")
    public ApiResponse<Void> sendMessage(@PathVariable Long groupId,
                                         @RequestBody Map<String, String> body,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        groupChatService.sendMessage(groupId, userId, body.get("content"));
        return ApiResponse.success();
    }

    @GetMapping("/{groupId}/messages")
    public ApiResponse<List<Map<String, Object>>> getMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(groupChatService.getMessages(groupId, limit));
    }
}