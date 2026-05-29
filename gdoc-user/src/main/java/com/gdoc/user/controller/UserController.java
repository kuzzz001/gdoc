package com.gdoc.user.controller;

import com.gdoc.common.result.ApiResponse;
import com.gdoc.model.dto.LoginRequest;
import com.gdoc.model.dto.LoginResponse;
import com.gdoc.model.dto.RegisterRequest;
import com.gdoc.model.dto.UserVO;
import com.gdoc.user.service.UserService;
import com.gdoc.model.dto.UpdateAvatarRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                                HttpServletResponse response) {
        LoginResponse loginResponse = userService.register(request);
        setTokenCookie(response, loginResponse.getToken());
        loginResponse.setToken(null);
        return ApiResponse.success("注册成功", loginResponse);
    }

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(request);
        setTokenCookie(response, loginResponse.getToken());
        loginResponse.setToken(null);
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        clearTokenCookie(response);
        SecurityContextHolder.clearContext();
        return ApiResponse.success("已退出登录");
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

    private void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("gdoc_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("gdoc_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }
}