package com.gdoc.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gdoc.common.result.ApiResponse;
import com.gdoc.document.service.SearchService;
import com.gdoc.model.dto.DocumentVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docs")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ApiResponse<IPage<DocumentVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        IPage<DocumentVO> result = searchService.search(userId, keyword, page, size);
        return ApiResponse.success(result);
    }
}