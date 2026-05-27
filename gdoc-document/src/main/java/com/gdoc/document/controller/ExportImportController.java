package com.gdoc.document.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.ExportService;
import com.gdoc.document.service.ImportService;
import com.gdoc.model.dto.DocumentVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/docs")
public class ExportImportController {

    private final ExportService exportService;
    private final ImportService importService;

    public ExportImportController(ExportService exportService, ImportService importService) {
        this.exportService = exportService;
        this.importService = importService;
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        byte[] data = exportService.exportPdf(id, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(data);
    }

    @GetMapping("/{id}/export/word")
    public ResponseEntity<byte[]> exportWord(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        byte[] data = exportService.exportWord(id, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.doc\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping("/{id}/export/markdown")
    public ResponseEntity<byte[]> exportMarkdown(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        byte[] data = exportService.exportMarkdown(id, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.md\"")
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .body(data);
    }

    @PostMapping("/import/markdown")
    public ApiResponse<DocumentVO> importMarkdown(
            @RequestParam String title,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        Long userId = (Long) authentication.getPrincipal();
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        DocumentVO doc = importService.importMarkdown(userId, title, content);
        return ApiResponse.success(doc);
    }
}