package com.example.bookstore.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.Constants;
import com.example.bookstore.common.Result;
import com.example.bookstore.entity.User;
import com.example.bookstore.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserManageController {

    private final UserMapper userMapper;

    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRole, role);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(1, "用户不存在");
        }
        user.setStatus(Constants.STATUS_DISABLED);
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(1, "用户不存在");
        }
        user.setStatus(Constants.STATUS_NORMAL);
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(1, "用户不存在");
        }
        String role = body.get("role");
        if (role == null || (!role.equals("user") && !role.equals("admin"))) {
            return Result.error(1, "无效的角色");
        }
        user.setRole(role);
        userMapper.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(1, "用户不存在");
        }
        userMapper.deleteById(id);
        return Result.success();
    }
}