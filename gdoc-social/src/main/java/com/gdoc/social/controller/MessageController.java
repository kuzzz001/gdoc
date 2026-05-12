package com.gdoc.social.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.model.dto.MessageVO;
import com.gdoc.model.dto.SendMessageRequest;
import com.gdoc.social.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ApiResponse<MessageVO> sendMessage(@Valid @RequestBody SendMessageRequest request,
                                              Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        MessageVO message = messageService.sendMessage(userId, request);
        return ApiResponse.success(message);
    }

    @GetMapping("/history/{friendId}")
    public ApiResponse<List<MessageVO>> getChatHistory(@PathVariable Long friendId,
                                                       @RequestParam(defaultValue = "50") int limit,
                                                       @RequestParam(defaultValue = "0") int offset,
                                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MessageVO> messages = messageService.getChatHistory(userId, friendId, limit, offset);
        return ApiResponse.success(messages);
    }

    @PostMapping("/read/{friendId}")
    public ApiResponse<Void> markAsRead(@PathVariable Long friendId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        messageService.markAsRead(userId, friendId);
        return ApiResponse.success(null);
    }

    @GetMapping("/unread/{friendId}")
    public ApiResponse<Integer> getUnreadCount(@PathVariable Long friendId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        int count = messageService.getUnreadCount(userId, friendId);
        return ApiResponse.success(count);
    }

    @GetMapping("/unread-total")
    public ApiResponse<Integer> getTotalUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        int count = messageService.getTotalUnreadCount(userId);
        return ApiResponse.success(count);
    }
}
