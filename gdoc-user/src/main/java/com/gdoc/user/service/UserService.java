package com.gdoc.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.model.dto.LoginRequest;
import com.gdoc.model.dto.LoginResponse;
import com.gdoc.model.dto.RegisterRequest;
import com.gdoc.model.dto.UserVO;
import com.gdoc.model.entity.GdocUser;
import com.gdoc.security.util.JwtUtils;
import com.gdoc.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public UserVO register(RegisterRequest request) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<GdocUser>()
                .eq(GdocUser::getUsername, request.getUsername()));
        if (exists) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        GdocUser user = new GdocUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        userMapper.insert(user);

        return toVO(user);
    }

    public LoginResponse login(LoginRequest request) {
        GdocUser user = userMapper.selectOne(new LambdaQueryWrapper<GdocUser>()
                .eq(GdocUser::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getAvatarUrl());
    }

    public UserVO getMe(Long userId) {
        GdocUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    public UserVO updateAvatar(Long userId, String avatarUrl) {
        GdocUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return toVO(user);
    }

    private UserVO toVO(GdocUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        return vo;
    }
}