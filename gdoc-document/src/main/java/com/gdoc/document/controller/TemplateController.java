package com.gdoc.document.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.TemplateService;
import com.gdoc.model.dto.TemplateCreateRequest;
import com.gdoc.model.dto.TemplateVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ApiResponse<TemplateVO> create(@Valid @RequestBody TemplateCreateRequest request,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(templateService.create(userId, request));
    }

    @GetMapping
    public ApiResponse<List<TemplateVO>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(templateService.list(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TemplateVO> getById(@PathVariable Long id) {
        return ApiResponse.success(templateService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        templateService.delete(id, userId);
        return ApiResponse.success();
    }
}