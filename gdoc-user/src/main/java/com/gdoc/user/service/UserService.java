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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ReentrantLock accountLock = new ReentrantLock();

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse register(RegisterRequest request) {
        String account = generateNextAccount();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        GdocUser user = new GdocUser();
        user.setUsername(account);
        user.setPassword(encodedPassword);
        user.setNickname(request.getNickname() != null ? request.getNickname() : "用户" + account);
        userMapper.insert(user);

        log.info("新用户注册，分配账号: {}", account);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        UserVO userVO = toVO(user);
        return new LoginResponse(token, userVO);
    }

    private String generateNextAccount() {
        accountLock.lock();
        try {
            Integer maxNo = userMapper.selectMaxAccountNo();
            int nextNo = (maxNo == null) ? 1 : maxNo + 1;

            for (int i = 0; i < 100; i++) {
                String candidate = String.format("%06d", nextNo);
                boolean exists = userMapper.exists(new LambdaQueryWrapper<GdocUser>()
                        .eq(GdocUser::getUsername, candidate));
                if (!exists) {
                    return candidate;
                }
                nextNo++;
            }
            throw new BusinessException(ResultCode.INTERNAL_ERROR);
        } finally {
            accountLock.unlock();
        }
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
        UserVO userVO = toVO(user);
        return new LoginResponse(token, userVO);
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
        vo.setAccountNo(user.getUsername());
        return vo;
    }
}