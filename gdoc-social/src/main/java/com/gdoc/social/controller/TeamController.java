package com.gdoc.social.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.social.service.TeamService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(teamService.create(userId, body.get("name"), body.get("description")));
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(teamService.list(userId));
    }

    @PostMapping("/{teamId}/members")
    public ApiResponse<Void> addMember(@PathVariable Long teamId,
                                       @RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        String role = (String) body.getOrDefault("role", "member");
        teamService.addMember(teamId, userId, role);
        return ApiResponse.success();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ApiResponse<Void> removeMember(@PathVariable Long teamId,
                                          @PathVariable Long userId) {
        teamService.removeMember(teamId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{teamId}/members")
    public ApiResponse<List<Map<String, Object>>> listMembers(@PathVariable Long teamId) {
        return ApiResponse.success(teamService.listMembers(teamId));
    }

    @DeleteMapping("/{teamId}")
    public ApiResponse<Void> delete(@PathVariable Long teamId,
                                    Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        teamService.delete(teamId, userId);
        return ApiResponse.success();
    }
}