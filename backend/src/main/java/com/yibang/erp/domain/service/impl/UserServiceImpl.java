package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.PageUtils;
import com.yibang.erp.domain.dto.UserQueryRequest;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.UserService;
import com.yibang.erp.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResult<User> getUserPage(UserQueryRequest queryRequest) {
        // 验证分页参数
        PageUtils.validatePageRequest(queryRequest);
        
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(queryRequest.getUsername())) {
            queryWrapper.like(User::getUsername, queryRequest.getUsername());
        }
        if (StringUtils.hasText(queryRequest.getRealName())) {
            queryWrapper.like(User::getRealName, queryRequest.getRealName());
        }
        if (StringUtils.hasText(queryRequest.getStatus())) {
            queryWrapper.eq(User::getStatus, queryRequest.getStatus());
        }
        if (StringUtils.hasText(queryRequest.getDepartment())) {
            queryWrapper.eq(User::getDepartment, queryRequest.getDepartment());
        }
        if (StringUtils.hasText(queryRequest.getEmail())) {
            queryWrapper.like(User::getEmail, queryRequest.getEmail());
        }
        if (StringUtils.hasText(queryRequest.getPhone())) {
            queryWrapper.like(User::getPhone, queryRequest.getPhone());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(User::getCreatedAt);
        
        // 获取总记录数
        long total = userRepository.selectCount(queryWrapper);
        
        if (total == 0) {
            return PageUtils.createEmptyPageResult(queryRequest);
        }
        
        // 添加分页条件（手动实现分页）
        queryWrapper.last(String.format("LIMIT %d, %d", 
            queryRequest.getOffset(), queryRequest.getLimit()));
        
        // 查询数据
        List<User> records = userRepository.selectList(queryWrapper);
        
        // 创建分页结果
        return PageUtils.createPageResult(records, total, queryRequest);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userRepository.selectOne(queryWrapper);
    }

    @Override
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (getUserByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 设置默认值
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(false);
        user.setLoginAttempts(0);
        user.setPasswordChangedAt(LocalDateTime.now());
        
        // 加密密码
        if (StringUtils.hasText(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        
        // 保存用户
        userRepository.insert(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        // 检查用户是否存在
        User existingUser = getUserById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户名是否重复（排除自己）
        User userWithSameUsername = getUserByUsername(user.getUsername());
        if (userWithSameUsername != null && !userWithSameUsername.getId().equals(user.getId())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 设置更新时间
        user.setUpdatedAt(LocalDateTime.now());
        
        // 不更新密码相关字段
        user.setPasswordHash(null);
        user.setPasswordChangedAt(null);
        user.setLoginAttempts(null);
        user.setLastLoginAt(null);
        user.setLastLoginIp(null);
        user.setLockedUntil(null);
        
        // 更新用户
        userRepository.updateById(user);
        return getUserById(user.getId());
    }

    @Override
    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id) > 0;
    }

    @Override
    public int batchDeleteUsers(List<Long> ids) {
        return userRepository.deleteBatchIds(ids);
    }

    @Override
    public boolean updateUserStatus(Long id, String status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.updateById(user) > 0;
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.updateById(user) > 0;
    }

    @Override
    public List<User> getAllUsers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(User::getCreatedAt);
        return userRepository.selectList(queryWrapper);
    }
}
