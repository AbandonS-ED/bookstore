package com.example.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.dto.RegisterDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.service.impl.UserServiceImpl;
import com.example.bookstore.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(SecurityUtils.encryptPassword("123456"));
        testUser.setRole("user");
        testUser.setStatus(1);
    }

    @Test
    void getUserByUsername_whenUserExists_returnsUser() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        User result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("user", result.getRole());
    }

    @Test
    void getUserByUsername_whenUserNotExists_returnsNull() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        User result = userService.getUserByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    void register_encryptsPasswordAndSaves() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setEmail("new@test.com");

        assertDoesNotThrow(() -> userService.register(dto));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("newuser", savedUser.getUsername());
        assertNotEquals("123456", savedUser.getPassword());
        assertTrue(SecurityUtils.matchesPassword("123456", savedUser.getPassword()));
    }

    @Test
    void login_withCorrectCredentials_returnsUsername() {
        String hashedPassword = SecurityUtils.encryptPassword("123456");
        testUser.setPassword(hashedPassword);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        var dto = new com.example.bookstore.dto.LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        String result = assertDoesNotThrow(() -> userService.login(dto));

        assertEquals("testuser", result);
    }

    @Test
    void login_withWrongPassword_throwsException() {
        testUser.setPassword(SecurityUtils.encryptPassword("123456"));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        var dto = new com.example.bookstore.dto.LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("wrongpassword");

        assertThrows(Exception.class, () -> userService.login(dto));
    }
}