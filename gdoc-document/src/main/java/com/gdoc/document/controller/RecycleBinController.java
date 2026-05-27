package com.gdoc.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.RecycleBinService;
import com.gdoc.model.dto.DocumentVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recycle-bin")
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    public RecycleBinController(RecycleBinService recycleBinService) {
        this.recycleBinService = recycleBinService;
    }

    @GetMapping
    public ApiResponse<IPage<DocumentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(recycleBinService.listDeleted(userId, page, size));
    }

    @PostMapping("/{docId}/restore")
    public ApiResponse<Void> restore(@PathVariable Long docId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        recycleBinService.restore(docId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{docId}")
    public ApiResponse<Void> permanentDelete(@PathVariable Long docId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        recycleBinService.permanentDelete(docId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> emptyBin(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        recycleBinService.emptyBin(userId);
        return ApiResponse.success();
    }
}