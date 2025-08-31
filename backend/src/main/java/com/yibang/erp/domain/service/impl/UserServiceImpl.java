package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.PageUtils;
import com.yibang.erp.domain.dto.UserListResponse;
import com.yibang.erp.domain.dto.UserQueryRequest;
import com.yibang.erp.domain.entity.Company;
import com.yibang.erp.domain.entity.PriceTier;
import com.yibang.erp.domain.entity.Role;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.CompanyService;
import com.yibang.erp.domain.service.RoleService;
import com.yibang.erp.domain.service.UserService;
import com.yibang.erp.infrastructure.repository.PriceTierRepository;
import com.yibang.erp.infrastructure.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PriceTierRepository priceTierRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private RoleService roleService;
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
        if (StringUtils.hasText(user.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
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

    @Override
    public PageResult<UserListResponse> getUserPageWithRoleAndCompany(UserQueryRequest queryRequest,Long belongCompanyId) {
        // 验证分页参数
        PageUtils.validatePageRequest(queryRequest);
        boolean isAdmin=  SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SYSTEM_ADMIN"));


        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(queryRequest.getUsername())) {
            queryWrapper.like(User::getUsername, queryRequest.getUsername());
        }

        if(!isAdmin){
            queryWrapper.eq(User::getSupplierCompanyId, belongCompanyId);
        }


        if (StringUtils.hasText(queryRequest.getPersonalCompanyName())) {
            queryWrapper.like(User::getRealName, queryRequest.getPersonalCompanyName());
        }
        if (StringUtils.hasText(queryRequest.getEmail())) {
            queryWrapper.like(User::getEmail, queryRequest.getEmail());
        }
        if (StringUtils.hasText(queryRequest.getPhone())) {
            queryWrapper.like(User::getPhone, queryRequest.getPhone());
        }
        if (StringUtils.hasText(queryRequest.getRoleName())) {
            // 这里需要通过角色名称查询，需要联查角色表
            // 暂时跳过，后续优化
        }
        if (StringUtils.hasText(queryRequest.getStatus())) {
            queryWrapper.eq(User::getStatus, queryRequest.getStatus());
        }
        if (queryRequest.getCompanyId() != null) {
            queryWrapper.eq(User::getCompanyId, queryRequest.getCompanyId());
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
        
        // 查询用户数据
        List<User> users = userRepository.selectList(queryWrapper);
        
        // 转换为UserListResponse
        List<UserListResponse> userResponses = users.stream()
            .map(this::convertToUserListResponse)
            .collect(Collectors.toList());
        
        // 创建分页结果
        return PageUtils.createPageResult(userResponses, total, queryRequest);
    }
    
    /**
     * 将User实体转换为UserListResponse
     */
    private UserListResponse convertToUserListResponse(User user) {
        UserListResponse response = new UserListResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setPersonalCompanyName(user.getRealName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setCompanyId(user.getCompanyId());
        response.setSupplierCompanyId(user.getSupplierCompanyId());
        response.setSalesCompanyId(user.getSalesCompanyId());
        response.setSalesType(user.getSalesType());

        
        // 设置价格分层信息
        response.setPriceTierId(user.getPriceTierId());
        
        // 如果有关联的价格分层，查询详细信息
        if (user.getPriceTierId() != null) {
            try {
                PriceTier priceTier = priceTierRepository.selectById(user.getPriceTierId());
                if (priceTier != null) {
                    response.setPriceTierName(priceTier.getTierName());
                    response.setPriceTierType(priceTier.getTierType());
                }
            } catch (Exception e) {
                // 记录日志但不影响主流程
                log.warn("查询用户价格分层信息失败，用户ID: {}, 价格分层ID: {}", user.getId(), user.getPriceTierId());
            }
        }
        
        // 这里可以添加角色名称和公司名称的查询逻辑

        if(user.getCompanyId()!=null){
            Long personCompanyId = user.getCompanyId();
            if (personCompanyId != null && personCompanyId > 0) {
                Company company =companyService.getCompanyById(personCompanyId);
                if(company!=null){
                    response.setCompanyName(company.getName());
                }

            }
        }
        //角色名称

        if(user.getRoleId()!=null){
            Role role = roleService.getRoleById(user.getRoleId());
            if (role != null) {
                response.setRoleName(role.getName());
            }
        }

        // 暂时设置为null，后续优化
        
        return response;
    }
}
