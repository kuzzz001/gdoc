package com.gdoc.document.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.TagService;
import com.gdoc.model.dto.DocumentTagRequest;
import com.gdoc.model.dto.TagCreateRequest;
import com.gdoc.model.dto.TagVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ApiResponse<TagVO> create(@Valid @RequestBody TagCreateRequest request,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(tagService.create(userId, request));
    }

    @GetMapping
    public ApiResponse<List<TagVO>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(tagService.list(userId));
    }

    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> delete(@PathVariable Long tagId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        tagService.delete(tagId, userId);
        return ApiResponse.success();
    }

    @PutMapping("/docs/{docId}")
    public ApiResponse<Void> tagDocument(@PathVariable Long docId,
                                         @RequestBody DocumentTagRequest request,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        tagService.tagDocument(docId, userId, request);
        return ApiResponse.success();
    }

    @GetMapping("/docs/{docId}")
    public ApiResponse<List<TagVO>> getDocumentTags(@PathVariable Long docId) {
        return ApiResponse.success(tagService.getDocumentTags(docId));
    }
}