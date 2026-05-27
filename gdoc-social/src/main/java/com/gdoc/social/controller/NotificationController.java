package com.gdoc.social.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.model.dto.NotificationVO;
import com.gdoc.social.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationVO>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(notificationService.list(userId));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(notificationService.unreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markRead(id, userId);
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllRead(userId);
        return ApiResponse.success();
    }
}