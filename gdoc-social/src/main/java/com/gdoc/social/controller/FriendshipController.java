package com.gdoc.social.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.model.dto.*;
import com.gdoc.social.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping("/search")
    public ApiResponse<List<UserVO>> searchUsers(@RequestParam String keyword, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<UserVO> users = friendshipService.searchUsers(keyword, userId);
        return ApiResponse.success(users);
    }

    @PostMapping("/add")
    public ApiResponse<Void> addFriend(@Valid @RequestBody AddFriendRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendshipService.addFriend(userId, request.getFriendId());
        return ApiResponse.success("好友请求已发送", null);
    }

    @PostMapping("/accept/{friendshipId}")
    public ApiResponse<Void> acceptFriend(@PathVariable Long friendshipId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendshipService.acceptFriend(userId, friendshipId);
        return ApiResponse.success("已接受好友请求", null);
    }

    @PostMapping("/reject/{friendshipId}")
    public ApiResponse<Void> rejectFriend(@PathVariable Long friendshipId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendshipService.rejectFriend(userId, friendshipId);
        return ApiResponse.success("已拒绝好友请求", null);
    }

    @GetMapping("/list")
    public ApiResponse<List<FriendVO>> getFriendList(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<FriendVO> friends = friendshipService.getFriendList(userId);
        return ApiResponse.success(friends);
    }

    @GetMapping("/pending")
    public ApiResponse<List<FriendVO>> getPendingRequests(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<FriendVO> requests = friendshipService.getPendingRequests(userId);
        return ApiResponse.success(requests);
    }

    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> deleteFriend(@PathVariable Long friendId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendshipService.deleteFriend(userId, friendId);
        return ApiResponse.success("已删除好友", null);
    }
}
