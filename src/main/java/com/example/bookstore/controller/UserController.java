package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.LoginDTO;
import com.example.bookstore.dto.PasswordUpdateDTO;
import com.example.bookstore.dto.RegisterDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.UserService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.util.JwtUtils;
import com.example.bookstore.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginDTO loginDTO) {
        String username = userService.login(loginDTO);
        User user = userService.getUserByUsername(username);
        String token = JwtUtils.generateToken(user.getId(), user.getRole());
        return Result.success(Map.of("token", token, "username", username));
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        Long userId = AuthContext.getCurrentUserId();
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        Long userId = AuthContext.getCurrentUserId();
        userService.updatePassword(userId, passwordUpdateDTO);
        return Result.success();
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody RegisterDTO registerDTO) {
        Long userId = AuthContext.getCurrentUserId();
        userService.updateProfile(userId, registerDTO);
        return Result.success();
    }
}