package com.yibang.erp.common.util;

import com.yibang.erp.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类
 * 提供安全相关的工具方法
 */
public class SecurityUtils {
    
    /**
     * 获取当前认证对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        return principal.toString();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getId();
        }
        
        return null;
    }
    
    /**
     * 获取当前用户对象
     */
    public static User getCurrentUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        
        return null;
    }
    
    /**
     * 检查用户是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * 检查用户是否具有指定角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * 检查用户是否具有指定权限
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(authority));
    }
    
    /**
     * 获取当前用户的权限列表
     */
    public static java.util.List<String> getCurrentUserAuthorities() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return java.util.Collections.emptyList();
        }
        
        return authentication.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取当前用户的角色列表
     */
    public static java.util.List<String> getCurrentUserRoles() {
        return getCurrentUserAuthorities().stream()
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring(5))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 清除安全上下文
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
