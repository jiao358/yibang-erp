package com.yibang.erp.security.service.impl;

import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.entity.Role;
import com.yibang.erp.domain.entity.Permission;
import com.yibang.erp.infrastructure.repository.UserRepository;
import com.yibang.erp.infrastructure.repository.RoleRepository;
import com.yibang.erp.infrastructure.repository.PermissionRepository;
import com.yibang.erp.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    @Override
    @Cacheable(value = "userPermissions", key = "#username")
    public boolean hasPermission(String username, String permission) {
        try {
            Set<String> userPermissions = getUserPermissions(username);
            return userPermissions.contains(permission);
        } catch (Exception e) {
            log.error("权限验证失败: username={}, permission={}", username, permission, e);
            return false;
        }
    }
    
    @Override
    public boolean hasAnyPermission(String username, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        
        Set<String> userPermissions = getUserPermissions(username);
        return permissions.stream().anyMatch(userPermissions::contains);
    }
    
    @Override
    public boolean hasAllPermissions(String username, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        
        Set<String> userPermissions = getUserPermissions(username);
        return permissions.stream().allMatch(userPermissions::contains);
    }
    
    @Override
    @Cacheable(value = "userPermissions", key = "#username")
    public Set<String> getUserPermissions(String username) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return Collections.emptySet();
            }
            
            Set<String> permissions = new HashSet<>();
            
            // 获取用户角色的权限
            if (user.getRoles() != null) {
                for (Role role : user.getRoles()) {
                    Set<String> rolePermissions = getRolePermissions(role.getId());
                    permissions.addAll(rolePermissions);
                }
            }
            
            // 获取用户直接权限
            if (user.getPermissions() != null) {
                for (Permission permission : user.getPermissions()) {
                    permissions.add(permission.getPermissionCode());
                }
            }
            
            log.debug("用户 {} 的权限: {}", username, permissions);
            return permissions;
            
        } catch (Exception e) {
            log.error("获取用户权限失败: username={}", username, e);
            return Collections.emptySet();
        }
    }
    
    @Override
    @Cacheable(value = "rolePermissions", key = "#roleId")
    public Set<String> getRolePermissions(Long roleId) {
        try {
            Role role = roleRepository.selectById(roleId);
            if (role == null || role.getPermissions() == null) {
                return Collections.emptySet();
            }
            
            return role.getPermissions().stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toSet());
                
        } catch (Exception e) {
            log.error("获取角色权限失败: roleId={}", roleId, e);
            return Collections.emptySet();
        }
    }
    
    @Override
    public boolean hasRole(String username, String role) {
        try {
            Set<String> userRoles = getUserRoles(username);
            return userRoles.contains(role);
        } catch (Exception e) {
            log.error("角色验证失败: username={}, role={}", username, role, e);
            return false;
        }
    }
    
    @Override
    public boolean hasAnyRole(String username, List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        
        Set<String> userRoles = getUserRoles(username);
        return roles.stream().anyMatch(userRoles::contains);
    }
    
    @Override
    @Cacheable(value = "userRoles", key = "#username")
    public Set<String> getUserRoles(String username) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null || user.getRoles() == null) {
                return Collections.emptySet();
            }
            
            return user.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());
                
        } catch (Exception e) {
            log.error("获取用户角色失败: username={}", username, e);
            return Collections.emptySet();
        }
    }
    
    @Override
    @CacheEvict(value = "userPermissions", key = "#username")
    public void refreshUserPermissions(String username) {
        log.info("刷新用户权限缓存: username={}", username);
    }
    
    @Override
    @CacheEvict(value = "rolePermissions", key = "#roleId")
    public void refreshRolePermissions(Long roleId) {
        log.info("刷新角色权限缓存: roleId={}", roleId);
    }
}
