package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.common.Constants;
import com.example.bookstore.dto.LoginDTO;
import com.example.bookstore.dto.PasswordUpdateDTO;
import com.example.bookstore.dto.ProfileUpdateDTO;
import com.example.bookstore.dto.RegisterDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.service.UserService;
import com.example.bookstore.util.SecurityUtils;
import com.example.bookstore.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public void register(RegisterDTO registerDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(1, "用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.STATUS_NORMAL);
        userMapper.insert(user);
    }

    @Override
    public String login(LoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(1, "用户名或密码错误");
        }

        if (!SecurityUtils.matchesPassword(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(1, "用户名或密码错误");
        }

        if (user.getStatus().equals(Constants.STATUS_DISABLED)) {
            throw new BusinessException(1, "账号已被禁用");
        }

        return user.getUsername();
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1, "用户不存在");
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setRole(user.getRole());
        userVO.setStatus(user.getStatus());
        userVO.setCreateTime(user.getCreateTime());
        return userVO;
    }

    @Override
    public void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1, "用户不存在");
        }

        if (!SecurityUtils.matchesPassword(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(1, "原密码错误");
        }

        if (!StringUtils.hasText(passwordUpdateDTO.getNewPassword())
                || passwordUpdateDTO.getNewPassword().length() < 6
                || passwordUpdateDTO.getNewPassword().length() > 20) {
            throw new BusinessException(1, "新密码6-20位");
        }

        user.setPassword(SecurityUtils.encryptPassword(passwordUpdateDTO.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateDTO profileUpdateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1, "用户不存在");
        }

        if (StringUtils.hasText(profileUpdateDTO.getEmail())) {
            user.setEmail(profileUpdateDTO.getEmail());
        }
        if (StringUtils.hasText(profileUpdateDTO.getPhone())) {
            user.setPhone(profileUpdateDTO.getPhone());
        }
        userMapper.updateById(user);
    }
}