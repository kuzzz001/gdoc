package com.gdoc.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.DocumentService;
import com.gdoc.model.dto.*;
import com.gdoc.security.annotation.DocPermission;
import com.gdoc.security.annotation.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ApiResponse<DocumentVO> create(@Valid @RequestBody DocumentCreateRequest request,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        DocumentVO doc = documentService.create(request, userId);
        return ApiResponse.success(doc);
    }

    @GetMapping
    public ApiResponse<IPage<DocumentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        IPage<DocumentVO> docPage = documentService.list(userId, page, size);
        return ApiResponse.success(docPage);
    }

    @GetMapping("/{id}")
    @RequirePermission(DocPermission.VIEWER)
    public ApiResponse<DocumentVO> getById(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        DocumentVO doc = documentService.getById(id, userId);
        return ApiResponse.success(doc);
    }

    @PutMapping("/{id}")
    @RequirePermission(DocPermission.EDITOR)
    public ApiResponse<DocumentVO> update(@PathVariable Long id,
                                          @Valid @RequestBody DocumentUpdateRequest request,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        DocumentVO doc = documentService.update(id, request, userId);
        return ApiResponse.success(doc);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(DocPermission.OWNER)
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.delete(id, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/share")
    @RequirePermission(DocPermission.OWNER)
    public ApiResponse<ShareVO> createShare(@PathVariable Long id,
                                            @Valid @RequestBody CreateShareRequest request,
                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ShareVO share = documentService.createShare(id, request, userId);
        return ApiResponse.success(share);
    }

    @GetMapping("/{id}/shares")
    @RequirePermission(DocPermission.OWNER)
    public ApiResponse<List<ShareVO>> listShares(@PathVariable Long id,
                                                  Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ShareVO> shares = documentService.listShares(id, userId);
        return ApiResponse.success(shares);
    }

    @DeleteMapping("/{id}/shares/{token}")
    @RequirePermission(DocPermission.OWNER)
    public ApiResponse<Void> revokeShare(@PathVariable Long id,
                                         @PathVariable String token,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.revokeShare(id, token, userId);
        return ApiResponse.success();
    }

    @GetMapping("/share/{token}")
    public ApiResponse<DocumentVO> getByShareToken(@PathVariable String token) {
        DocumentVO doc = documentService.getByShareToken(token, null);
        return ApiResponse.success(doc);
    }

    @GetMapping("/{id}/collaborators")
    @RequirePermission(DocPermission.VIEWER)
    public ApiResponse<List<CollaboratorVO>> listCollaborators(@PathVariable Long id,
                                                                Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<CollaboratorVO> collaborators = documentService.listCollaborators(id, userId);
        return ApiResponse.success(collaborators);
    }

    @PostMapping("/{id}/collaborators")
    @RequirePermission(DocPermission.OWNER)
    public ApiResponse<CollaboratorVO> addCollaborator(@PathVariable Long id,
                                                        @Valid @RequestBody AddCollaboratorRequest request,
                                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CollaboratorVO collaborator = documentService.addCollaborator(id, request, userId);
        return ApiResponse.success(collaborator);
    }

    @DeleteMapping("/{id}/collaborators/{targetUserId}")
    @RequirePermission(DocPermission.OWNER)
    public ApiResponse<Void> removeCollaborator(@PathVariable Long id,
                                                @PathVariable Long targetUserId,
                                                Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.removeCollaborator(id, targetUserId, userId);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/collaborators/{targetUserId}")
    @RequirePermission(DocPermission.OWNER)
    public ApiResponse<CollaboratorVO> updateCollaboratorRole(@PathVariable Long id,
                                                               @PathVariable Long targetUserId,
                                                               @Valid @RequestBody UpdateCollaboratorRoleRequest request,
                                                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CollaboratorVO collaborator = documentService.updateCollaboratorRole(id, targetUserId, request, userId);
        return ApiResponse.success(collaborator);
    }

    @GetMapping("/{id}/history/snapshots")
    @RequirePermission(DocPermission.VIEWER)
    public ApiResponse<List<SnapshotVO>> listSnapshots(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        List<SnapshotVO> snapshots = documentService.listSnapshots(id, page, size);
        return ApiResponse.success(snapshots);
    }

    @GetMapping("/{id}/history/snapshots/{version}")
    @RequirePermission(DocPermission.VIEWER)
    public ApiResponse<String> getSnapshotContent(@PathVariable Long id,
                                                   @PathVariable int version) {
        String content = documentService.getSnapshotContent(id, version);
        return ApiResponse.success(content);
    }

    @PostMapping("/{id}/history/rollback/{version}")
    @RequirePermission(DocPermission.OWNER)
    public ApiResponse<Void> rollback(@PathVariable Long id,
                                      @PathVariable int version,
                                      Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.rollbackToVersion(id, version, userId);
        return ApiResponse.success();
    }
}
