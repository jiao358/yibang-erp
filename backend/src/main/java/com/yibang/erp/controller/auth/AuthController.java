package com.yibang.erp.controller.auth;

import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.entity.Role;
import com.yibang.erp.domain.service.UserService;
import com.yibang.erp.domain.service.RoleService;
import com.yibang.erp.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder, UserService userService, RoleService roleService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.roleService = roleService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("=== 开始用户登录流程 ===");
        System.out.println("用户名: " + loginRequest.getUsername());
        System.out.println("密码长度: " + (loginRequest.getPassword() != null ? loginRequest.getPassword().length() : "null"));
        
        try {
            // 直接加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            System.out.println("用户存在: " + userDetails.getUsername());
            System.out.println("用户权限: " + userDetails.getAuthorities());
            
            // 直接验证密码
            if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
                System.out.println("密码验证失败");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "认证失败");
                response.put("message", "用户名或密码错误");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("密码验证成功！");
            
                            // 从数据库查询用户的真实角色信息
                List<String> roles = new ArrayList<>();
                User userEntity = userService.getUserByUsername(loginRequest.getUsername());
                if (userEntity != null && userEntity.getRoleId() != null) {
                    // 根据角色ID查询角色信息
                    try {
                        Role roleEntity = roleService.getRoleById(userEntity.getRoleId());
                        if (roleEntity != null) {
                            roles.add(roleEntity.getName());
                            System.out.println("从数据库获取到角色: " + roleEntity.getName());
                        } else {
                            // 如果角色不存在，使用默认角色
                            roles.add("USER");
                            System.out.println("角色不存在，使用默认角色: USER");
                        }
                    } catch (Exception e) {
                        System.out.println("查询角色信息失败: " + e.getMessage());
                        roles.add("USER");
                    }
                } else {
                    // 如果无法获取角色信息，使用默认角色
                    roles.add("USER");
                    System.out.println("无法获取角色信息，使用默认角色: USER");
                }
                System.out.println("最终的角色信息: " + roles);
            
            // 生成JWT Token，使用真实的用户信息
            String token = jwtUtil.generateToken(
                userDetails.getUsername(), 
                userEntity.getId(), // 使用数据库中的真实用户ID
                userEntity.getCompanyId(), // 使用数据库中的真实公司ID
                roles // 传入真实的角色信息
            );
            System.out.println("JWT Token生成完成");

            // 构建用户信息 - 从数据库查询真实数据
            Map<String, Object> user = new HashMap<>();
            
            // 使用之前已经查询的userEntity
            if (userEntity != null) {
                user.put("id", userEntity.getId());
                user.put("username", userEntity.getUsername());
                user.put("realName", userEntity.getRealName());
                user.put("email", userEntity.getEmail());
                user.put("roleId", userEntity.getRoleId());
                user.put("companyId", userEntity.getCompanyId());
                user.put("status", userEntity.getStatus());
                
                System.out.println("=== 从数据库获取到用户信息 ===");
                System.out.println("用户ID: " + userEntity.getId());
                System.out.println("真实姓名: " + userEntity.getRealName());
                System.out.println("邮箱: " + userEntity.getEmail());
                System.out.println("角色ID: " + userEntity.getRoleId());
                System.out.println("公司ID: " + userEntity.getCompanyId());
            } else {
                // 如果用户不存在，抛出异常（不应该发生，因为前面已经验证了用户存在）
                throw new RuntimeException("用户数据异常：无法从数据库获取用户信息");
            }
            
            // 用户信息已从数据库获取完成
            System.out.println("=== 用户信息获取完成 ===");
            System.out.println("当前用户: " + userDetails.getUsername());
            System.out.println("用户信息已从数据库获取，无硬编码数据");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("user", user);
            response.put("roles", roles); // 使用真实的角色信息
            response.put("message", "登录成功");
            response.put("timestamp", System.currentTimeMillis());

            System.out.println("=== 登录流程完成 ===");
            return ResponseEntity.ok(response);
            
        } catch (UsernameNotFoundException e) {
            System.out.println("=== 用户不存在 ===");
            System.out.println("异常消息: " + e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "认证失败");
            response.put("message", "用户名或密码错误");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.out.println("=== 其他异常 ===");
            System.out.println("异常类型: " + e.getClass().getSimpleName());
            System.out.println("异常消息: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "系统错误");
            response.put("message", "登录过程中发生系统错误");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 验证Token
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestBody TokenVerifyRequest request) {
        try {
            String username = jwtUtil.getUsernameFromToken(request.getToken());
            Long userId = jwtUtil.getUserIdFromToken(request.getToken());
            Long companyId = jwtUtil.getCompanyIdFromToken(request.getToken());
            List<String> roles = jwtUtil.getRolesFromToken(request.getToken());
            
            boolean isValid = jwtUtil.validateToken(request.getToken(), username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("username", username);
            response.put("userId", userId);
            response.put("companyId", companyId);
            response.put("roles", roles);
            
            // 如果token有效，尝试从数据库获取更多用户信息
            if (isValid) {
                User userEntity = userService.getUserByUsername(username);
                if (userEntity != null) {
                    response.put("realName", userEntity.getRealName());
                    response.put("email", userEntity.getEmail());
                    response.put("status", userEntity.getStatus());
                }
            }
            
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", "Token验证失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少有效的Authorization头"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token无效"));
            }

            // 从数据库获取完整的用户信息
            User userEntity = userService.getUserByUsername(username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities());
            response.put("enabled", userDetails.isEnabled());
            
            // 如果数据库中有用户信息，则添加更多详细信息
            if (userEntity != null) {
                response.put("userId", userEntity.getId());
                response.put("realName", userEntity.getRealName());
                response.put("email", userEntity.getEmail());
                response.put("roleId", userEntity.getRoleId());
                response.put("companyId", userEntity.getCompanyId());
                response.put("status", userEntity.getStatus());
                response.put("department", userEntity.getDepartment());
                response.put("position", userEntity.getPosition());
            }
            
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "获取用户信息失败");
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // 清除Spring Security上下文
        SecurityContextHolder.clearContext();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取测试用户列表（开发阶段使用）
     */
    @GetMapping("/test-users")
    public ResponseEntity<Map<String, Object>> getTestUsers() {
        Map<String, Object> response = new HashMap<>();
        
        // 从数据库获取所有用户信息
        List<User> allUsers = userService.getAllUsers();
        response.put("testUsers", allUsers);
        response.put("message", "从数据库获取的用户列表");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少有效的Authorization头"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token无效"));
            }

            // 生成新的Token，需要从原token中提取角色信息
            List<String> roles = jwtUtil.getRolesFromToken(token);
            if (roles == null || roles.isEmpty()) {
                // 如果没有角色信息，使用默认角色
                roles = List.of("USER");
            }
            String newToken = jwtUtil.generateToken(username, 1L, 1L, roles);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("username", username);
            response.put("message", "Token刷新成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Token刷新失败");
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 登录请求DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * Token验证请求DTO
     */
    public static class TokenVerifyRequest {
        private String token;

        // Getters and Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
