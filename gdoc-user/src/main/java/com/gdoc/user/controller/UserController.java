package com.gdoc.user.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.model.dto.LoginRequest;
import com.gdoc.model.dto.LoginResponse;
import com.gdoc.model.dto.RegisterRequest;
import com.gdoc.model.dto.UserVO;
import com.gdoc.user.service.UserService;
import com.gdoc.model.dto.UpdateAvatarRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public ApiResponse<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        UserVO user = userService.register(request);
        return ApiResponse.success("注册成功", user);
    }

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = userService.login(request);
        return ApiResponse.success(loginResponse);
    }

    @GetMapping("/user/me")
    public ApiResponse<UserVO> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserVO user = userService.getMe(userId);
        return ApiResponse.success(user);
    }

    @PutMapping("/user/avatar")
    public ApiResponse<UserVO> updateAvatar(Authentication authentication, @RequestBody UpdateAvatarRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserVO user = userService.updateAvatar(userId, request.getAvatarUrl());
        return ApiResponse.success("头像更新成功", user);
    }
}