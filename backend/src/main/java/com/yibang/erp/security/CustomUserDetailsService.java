package com.yibang.erp.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义用户详情服务
 * 开发阶段使用硬编码用户，后续会替换为数据库查询
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, UserDetails> userDetailsMap = new HashMap<>();

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 初始化用户数据
     */
    @PostConstruct
    public void initUsers() {
        System.out.println("=== 开始初始化用户数据 ===");
        System.out.println("使用的密码编码器: " + passwordEncoder.getClass().getSimpleName());
        
        // 临时生成正确的BCrypt加密密码
        System.out.println("=== 生成正确的BCrypt加密密码 ===");
        
        // 系统管理员 - admin123
        String encodedAdminPassword = passwordEncoder.encode("admin123");
        System.out.println("admin密码: admin123 -> 编码后: " + encodedAdminPassword);
        
        // 供应链管理员 - supply123
        String encodedSupplyPassword = passwordEncoder.encode("supply123");
        System.out.println("supply_admin密码: supply123 -> 编码后: " + encodedSupplyPassword);
        
        // 供应链操作员 - operator123
        String encodedOperatorPassword = passwordEncoder.encode("operator123");
        System.out.println("supply_operator密码: operator123 -> 编码后: " + encodedOperatorPassword);
        
        // 销售用户 - sales123
        String encodedSalesPassword = passwordEncoder.encode("sales123");
        System.out.println("sales_user密码: sales123 -> 编码后: " + encodedSalesPassword);
        
        // 普通用户 - user123
        String encodedUserPassword = passwordEncoder.encode("user123");
        System.out.println("user密码: user123 -> 编码后: " + encodedUserPassword);
        
        System.out.println("=== 密码生成完成 ===");
        
        UserDetails admin = User.builder()
                .username("admin")
                .password(encodedAdminPassword)
                .authorities(Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
        
        UserDetails supplyChainAdmin = User.builder()
                .username("supply_admin")
                .password(encodedSupplyPassword)
                .authorities(Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_SUPPLY_CHAIN_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
        
        UserDetails supplyChainOperator = User.builder()
                .username("supply_operator")
                .password(encodedOperatorPassword)
                .authorities(Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_SUPPLY_CHAIN_OPERATOR"),
                    new SimpleGrantedAuthority("ROLE_USER")
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
        
        UserDetails salesUser = User.builder()
                .username("sales_user")
                .password(encodedSalesPassword)
                .authorities(Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_SALES"),
                    new SimpleGrantedAuthority("ROLE_USER")
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
        
        UserDetails user = User.builder()
                .username("user")
                .password(encodedUserPassword)
                .authorities(Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_USER")
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
        
        // 将用户添加到Map中
        userDetailsMap.put("admin", admin);
        userDetailsMap.put("supply_admin", supplyChainAdmin);
        userDetailsMap.put("supply_operator", supplyChainOperator);
        userDetailsMap.put("sales_user", salesUser);
        userDetailsMap.put("user", user);
        
        System.out.println("=== 用户初始化完成 ===");
        System.out.println("已创建用户数量: " + userDetailsMap.size());
        userDetailsMap.keySet().forEach(username -> System.out.println("用户: " + username));
        System.out.println("=====================");
        
        // 验证密码匹配
        System.out.println("=== 密码验证测试 ===");
        System.out.println("supply_admin密码匹配: " + passwordEncoder.matches("supply123", encodedSupplyPassword));
        System.out.println("admin密码匹配: " + passwordEncoder.matches("admin123", encodedAdminPassword));
        System.out.println("=====================");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = userDetailsMap.get(username);
        
        if (userDetails == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        return userDetails;
    }
    
    /**
     * 获取所有测试用户信息（开发阶段使用）
     */
    public Map<String, String> getTestUsers() {
        Map<String, String> testUsers = new HashMap<>();
        testUsers.put("admin", "admin123");
        testUsers.put("supply_admin", "supply123");
        testUsers.put("supply_operator", "operator123");
        testUsers.put("sales_user", "sales123");
        testUsers.put("user", "user123");
        return testUsers;
    }
}
