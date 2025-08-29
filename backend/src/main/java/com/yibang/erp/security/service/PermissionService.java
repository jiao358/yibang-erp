package com.yibang.erp.security.service;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 * 负责权限验证和权限管理
 */
public interface PermissionService {
    
    /**
     * 检查用户是否拥有指定权限
     *
     * @param username 用户名
     * @param permission 权限标识符
     * @return 是否拥有权限
     */
    boolean hasPermission(String username, String permission);
    
    /**
     * 检查用户是否拥有指定权限列表中的任意一个
     *
     * @param username 用户名
     * @param permissions 权限标识符列表
     * @return 是否拥有权限
     */
    boolean hasAnyPermission(String username, List<String> permissions);
    
    /**
     * 检查用户是否拥有指定权限列表中的所有权限
     *
     * @param username 用户名
     * @param permissions 权限标识符列表
     * @return 是否拥有权限
     */
    boolean hasAllPermissions(String username, List<String> permissions);
    
    /**
     * 获取用户的所有权限
     *
     * @param username 用户名
     * @return 权限集合
     */
    Set<String> getUserPermissions(String username);
    
    /**
     * 获取角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限集合
     */
    Set<String> getRolePermissions(Long roleId);
    
    /**
     * 检查用户是否拥有指定角色
     *
     * @param username 用户名
     * @param role 角色标识符
     * @return 是否拥有角色
     */
    boolean hasRole(String username, String role);
    
    /**
     * 检查用户是否拥有指定角色列表中的任意一个
     *
     * @param username 用户名
     * @param roles 角色标识符列表
     * @return 是否拥有角色
     */
    boolean hasAnyRole(String username, List<String> roles);
    
    /**
     * 获取用户的所有角色
     *
     * @param username 用户名
     * @return 角色集合
     */
    Set<String> getUserRoles(String username);
    
    /**
     * 刷新用户权限缓存
     *
     * @param username 用户名
     */
    void refreshUserPermissions(String username);
    
    /**
     * 刷新角色权限缓存
     *
     * @param roleId 角色ID
     */
    void refreshRolePermissions(Long roleId);
}
