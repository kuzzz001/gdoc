package com.gdoc.document.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.CommentService;
import com.gdoc.model.dto.CommentCreateRequest;
import com.gdoc.model.dto.CommentVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents/{docId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<CommentVO> create(@PathVariable Long docId,
                                          @Valid @RequestBody CommentCreateRequest request,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(commentService.create(docId, userId, request));
    }

    @GetMapping
    public ApiResponse<List<CommentVO>> list(@PathVariable Long docId) {
        return ApiResponse.success(commentService.list(docId));
    }

    @PutMapping("/{commentId}/resolve")
    public ApiResponse<Void> resolve(@PathVariable Long docId,
                                      @PathVariable Long commentId,
                                      @RequestParam Integer resolved) {
        commentService.resolve(commentId, resolved);
        return ApiResponse.success();
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(@PathVariable Long docId,
                                     @PathVariable Long commentId) {
        commentService.delete(commentId);
        return ApiResponse.success();
    }

    @PostMapping("/{commentId}/reply")
    public ApiResponse<CommentVO> reply(@PathVariable Long docId,
                                         @PathVariable Long commentId,
                                         @RequestParam String content,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(commentService.reply(commentId, userId, content));
    }
}