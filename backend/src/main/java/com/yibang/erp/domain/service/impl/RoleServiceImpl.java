package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Role;
import com.yibang.erp.domain.service.RoleService;
import com.yibang.erp.infrastructure.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public IPage<Role> getRolePage(Page<Role> page, String name, String status) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Role::getName, name);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Role::getStatus, status);
        }
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Role::getCreatedAt);


        //增加count
        Long totalCount= roleRepository.selectCount(queryWrapper);
        page.setTotal(totalCount);

        return roleRepository.selectPage(page, queryWrapper);
    }

    @Override
    public Role getRoleById(Long id) {
        return roleRepository.selectById(id);
    }

    @Override
    public Role getRoleByName(String name) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getName, name);
        return roleRepository.selectOne(queryWrapper);
    }

    @Override
    public Role createRole(Role role) {
        // 检查角色名称是否已存在
        if (getRoleByName(role.getName()) != null) {
            throw new RuntimeException("角色名称已存在");
        }
        
        // 设置默认值
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setDeleted(false);
        role.setIsSystem(false);
        
        // 保存角色
        roleRepository.insert(role);
        return role;
    }

    @Override
    public Role updateRole(Role role) {
        // 检查角色是否存在
        Role existingRole = getRoleById(role.getId());
        if (existingRole == null) {
            throw new RuntimeException("角色不存在");
        }
        
        // 检查角色名称是否重复（排除自己）
        Role roleWithSameName = getRoleByName(role.getName());
        if (roleWithSameName != null && !roleWithSameName.getId().equals(role.getId())) {
            throw new RuntimeException("角色名称已存在");
        }
        
       /* // 系统角色不允许修改名称和描述
        if (existingRole.getIsSystem()) {
            role.setName(null);
            role.setDescription(null);
        }
        */
        // 设置更新时间
        role.setUpdatedAt(LocalDateTime.now());
        
        // 更新角色
        roleRepository.updateById(role);
        return getRoleById(role.getId());
    }

    @Override
    public boolean deleteRole(Long id) {
        Role role = getRoleById(id);
        if (role != null && role.getIsSystem()) {
//            throw new RuntimeException("系统角色不允许删除");
        }
        return roleRepository.deleteById(id) > 0;
    }

    @Override
    public int batchDeleteRoles(List<Long> ids) {
        // 检查是否包含系统角色
        List<Role> roles = roleRepository.selectBatchIds(ids);
        for (Role role : roles) {
            if (role.getIsSystem()) {
//                throw new RuntimeException("系统角色不允许删除");
            }
        }
        return roleRepository.deleteBatchIds(ids);
    }

    @Override
    public boolean updateRoleStatus(Long id, String status) {
        Role role = getRoleById(id);
        if (role != null && role.getIsSystem()) {
            throw new RuntimeException("系统角色状态不允许修改");
        }
        
        Role updateRole = new Role();
        updateRole.setId(id);
        updateRole.setStatus(status);
        updateRole.setUpdatedAt(LocalDateTime.now());
        
        return roleRepository.updateById(updateRole) > 0;
    }

    @Override
    public List<Role> getAllRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Role::getCreatedAt);
        return roleRepository.selectList(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getAllActiveRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getStatus, "ACTIVE")
                   .eq(Role::getDeleted, false)
                   .orderByAsc(Role::getName);
        
        List<Role> roles = roleRepository.selectList(queryWrapper);
        return roles.stream()
            .map(role -> {
                Map<String, Object> roleMap = new HashMap<>();
                roleMap.put("id", role.getId());
                roleMap.put("name", role.getName());
                roleMap.put("description", role.getDescription());
                roleMap.put("supplierScope", role.getSupplierScope());
                return roleMap;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<Role> getActiveRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getStatus, "ACTIVE")
                   .eq(Role::getDeleted, false)
                   .orderByAsc(Role::getName);
        return roleRepository.selectList(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getRolesByName(String name) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Role::getName, name);
        List<Role> roles = roleRepository.selectList(queryWrapper);
        return roles.stream()
            .map(role -> {
                Map<String, Object> roleMap = new HashMap<>();
                roleMap.put("id", role.getId());
                roleMap.put("name", role.getName());
                roleMap.put("description", role.getDescription());
                roleMap.put("supplierScope", role.getSupplierScope());
                return roleMap;
            })
            .collect(Collectors.toList());
    }
}
