package com.gdoc.history.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.history.entity.GdocOperationLog;
import com.gdoc.history.entity.GdocSnapshot;
import com.gdoc.history.service.HistoryService;
import com.gdoc.security.annotation.DocPermission;
import com.gdoc.security.annotation.RequirePermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docs/{docId}/versions")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/snapshots")
    @RequirePermission(DocPermission.VIEWER)
    public ApiResponse<List<GdocSnapshot>> getSnapshots(
            @PathVariable Long docId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(historyService.getSnapshots(docId, page, size));
    }

    @GetMapping("/operations")
    @RequirePermission(DocPermission.VIEWER)
    public ApiResponse<List<GdocOperationLog>> getOperationLogs(
            @PathVariable Long docId,
            @RequestParam int fromVersion,
            @RequestParam int toVersion) {
        return ApiResponse.success(historyService.getOperationLogs(docId, fromVersion, toVersion));
    }

    @GetMapping("/{version}")
    @RequirePermission(DocPermission.VIEWER)
    public ApiResponse<Map<String, Object>> getContentAtVersion(
            @PathVariable Long docId,
            @PathVariable int version) {
        String content = historyService.getContentAtVersion(docId, version);
        return ApiResponse.success(Map.of("docId", docId, "version", version, "content", content));
    }

    @GetMapping("/compare")
    @RequirePermission(DocPermission.VIEWER)
    public ApiResponse<Map<String, Object>> compareVersions(
            @PathVariable Long docId,
            @RequestParam int versionA,
            @RequestParam int versionB) {
        List<Map<String, Object>> diffs = historyService.compareVersions(docId, versionA, versionB);
        return ApiResponse.success(Map.of(
                "docId", docId,
                "versionA", versionA,
                "versionB", versionB,
                "diffs", diffs
        ));
    }

    @PostMapping("/rollback/{version}")
    @RequirePermission(DocPermission.EDITOR)
    public ApiResponse<Void> rollback(
            @PathVariable Long docId,
            @PathVariable int version) {
        historyService.rollbackToVersion(docId, version);
        return ApiResponse.success("已回滚到版本 " + version);
    }
}