package com.yibang.erp.security;

import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义用户详情服务
 * 从数据库读取用户数据
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // 从数据库查询用户
            User user = userService.getUserByUsername(username);
            
            if (user == null) {
                throw new UsernameNotFoundException("用户不存在: " + username);
            }
            
            // 检查用户状态
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new UsernameNotFoundException("用户状态异常: " + user.getStatus());
            }
            
            // 构建权限列表
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            
            // 根据角色ID添加权限（这里简化处理，实际应该查询角色表）
            if (user.getRoleId() != null) {
                if (user.getRoleId() == 1) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));
                } else if (user.getRoleId() == 2) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_SUPPLIER_ADMIN"));
                } else if (user.getRoleId() == 3) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_SALES"));
                } else {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
            }
            
            // 添加默认用户权限
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            // 构建UserDetails对象
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPasswordHash()) // 使用数据库中的passwordHash字段
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
                    
        } catch (Exception e) {
            throw new UsernameNotFoundException("加载用户信息失败: " + e.getMessage());
        }
    }
}
