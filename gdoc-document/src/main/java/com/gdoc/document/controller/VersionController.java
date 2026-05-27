package com.gdoc.document.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.VersionService;
import com.gdoc.model.entity.GdocDocumentVersion;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents/{docId}/versions")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @PostMapping
    public ApiResponse<GdocDocumentVersion> createVersion(
            @PathVariable Long docId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String content = body.get("content");
        String name = body.get("name");
        return ApiResponse.success(versionService.createVersion(docId, content, name, userId));
    }

    @GetMapping
    public ApiResponse<List<GdocDocumentVersion>> listVersions(@PathVariable Long docId) {
        return ApiResponse.success(versionService.listVersions(docId));
    }

    @GetMapping("/{versionNumber}")
    public ApiResponse<GdocDocumentVersion> getVersion(
            @PathVariable Long docId,
            @PathVariable Integer versionNumber) {
        return ApiResponse.success(versionService.getVersion(docId, versionNumber));
    }

    @PutMapping("/{versionId}/name")
    public ApiResponse<Void> renameVersion(
            @PathVariable Long versionId,
            @RequestBody Map<String, String> body) {
        versionService.renameVersion(versionId, body.get("name"));
        return ApiResponse.success();
    }
}