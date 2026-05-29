package com.gdoc.user.service;

import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.model.dto.LoginRequest;
import com.gdoc.model.dto.LoginResponse;
import com.gdoc.model.dto.RegisterRequest;
import com.gdoc.model.dto.UserVO;
import com.gdoc.model.entity.GdocUser;
import com.gdoc.security.util.JwtUtils;
import com.gdoc.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private static final String TEST_JWT = "eyJhbGciOiJIUzI1NiJ9.test";

    @Nested
    class RegisterTests {

        private RegisterRequest request;

        @BeforeEach
        void setUp() {
            request = new RegisterRequest();
            request.setPassword("password123");
            request.setNickname("测试用户");
        }

        @Test
        void shouldRegisterSuccessfully() {
            when(userMapper.selectMaxAccountNo()).thenReturn(100);
            when(userMapper.exists(any())).thenReturn(false);
            when(userMapper.insert(any(GdocUser.class))).thenReturn(1);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
            when(jwtUtils.generateToken(any(), anyString())).thenReturn(TEST_JWT);

            LoginResponse response = userService.register(request);

            assertNotNull(response);
            assertEquals(TEST_JWT, response.getToken());
            assertEquals("000101", response.getUser().getUsername());
        }

        @Test
        void shouldAssignFirstAccountNumber() {
            when(userMapper.selectMaxAccountNo()).thenReturn(null);
            when(userMapper.exists(any())).thenReturn(false);
            when(userMapper.insert(any(GdocUser.class))).thenReturn(1);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
            when(jwtUtils.generateToken(any(), anyString())).thenReturn(TEST_JWT);

            LoginResponse response = userService.register(request);

            assertEquals("000001", response.getUser().getUsername());
        }

        @Test
        void shouldEncryptPassword() {
            when(userMapper.selectMaxAccountNo()).thenReturn(1);
            when(userMapper.exists(any())).thenReturn(false);
            when(userMapper.insert(any(GdocUser.class))).thenReturn(1);
            when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
            when(jwtUtils.generateToken(any(), anyString())).thenReturn(TEST_JWT);

            userService.register(request);

            verify(passwordEncoder).encode("password123");
        }
    }

    @Nested
    class LoginTests {

        private LoginRequest request;
        private GdocUser mockUser;

        @BeforeEach
        void setUp() {
            request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password123");

            mockUser = new GdocUser();
            mockUser.setId(1L);
            mockUser.setUsername("testuser");
            mockUser.setPassword("hashed_password");
            mockUser.setNickname("测试用户");
        }

        @Test
        void shouldLoginSuccessfully() {
            when(userMapper.selectOne(any())).thenReturn(mockUser);
            when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
            when(jwtUtils.generateToken(1L, "testuser")).thenReturn(TEST_JWT);

            LoginResponse response = userService.login(request);

            assertNotNull(response);
            assertEquals(TEST_JWT, response.getToken());
            assertEquals(1L, response.getUser().getId());
            assertEquals("testuser", response.getUser().getUsername());
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(userMapper.selectOne(any())).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.login(request));
            assertEquals(ResultCode.USER_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        void shouldThrowWhenPasswordMismatch() {
            when(userMapper.selectOne(any())).thenReturn(mockUser);
            when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.login(request));
            assertEquals(ResultCode.PASSWORD_ERROR.getCode(), ex.getCode());
        }
    }

    @Nested
    class GetMeTests {

        @Test
        void shouldReturnUserInfo() {
            GdocUser user = new GdocUser();
            user.setId(1L);
            user.setUsername("testuser");
            user.setNickname("测试");
            user.setEmail("test@test.com");
            user.setAvatarUrl("/avatar.png");

            when(userMapper.selectById(1L)).thenReturn(user);

            UserVO vo = userService.getMe(1L);

            assertNotNull(vo);
            assertEquals(1L, vo.getId());
            assertEquals("testuser", vo.getUsername());
            assertEquals("测试", vo.getNickname());
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.getMe(999L));
            assertEquals(ResultCode.USER_NOT_FOUND.getCode(), ex.getCode());
        }
    }
}