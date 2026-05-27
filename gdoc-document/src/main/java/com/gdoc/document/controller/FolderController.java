package com.gdoc.document.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.FolderService;
import com.gdoc.model.dto.FolderCreateRequest;
import com.gdoc.model.dto.FolderVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public ApiResponse<FolderVO> create(@Valid @RequestBody FolderCreateRequest request,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(folderService.create(userId, request));
    }

    @GetMapping
    public ApiResponse<List<FolderVO>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(folderService.list(userId));
    }

    @PutMapping("/{id}/rename")
    public ApiResponse<Void> rename(@PathVariable Long id, @RequestParam String name,
                                      Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        folderService.rename(id, userId, name);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        folderService.delete(id, userId);
        return ApiResponse.success();
    }
}