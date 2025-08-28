package com.yibang.erp.controller.auth;

import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.security.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
            // 先验证用户是否存在
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            System.out.println("用户存在，开始认证...");
            System.out.println("用户详情: " + userDetails.getUsername() + ", 权限: " + userDetails.getAuthorities());
            
            // 创建认证Token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            );
            System.out.println("认证Token创建完成");
            
            // 认证用户
            System.out.println("开始调用AuthenticationManager.authenticate...");
            Authentication authentication = authenticationManager.authenticate(authToken);
            System.out.println("认证成功！");
            System.out.println("认证结果: " + authentication.getName() + ", 权限: " + authentication.getAuthorities());
            
            // 获取认证后的用户详情
            UserDetails authenticatedUserDetails = (UserDetails) authentication.getPrincipal();
            System.out.println("认证后的用户详情: " + authenticatedUserDetails.getUsername());
            
            // 生成JWT Token（开发阶段使用硬编码的用户ID和公司ID）
            String token = jwtUtil.generateToken(
                authenticatedUserDetails.getUsername(), 
                1L, // 开发阶段硬编码用户ID
                1L  // 开发阶段硬编码公司ID
            );
            System.out.println("JWT Token生成完成");

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", authenticatedUserDetails.getUsername());
            response.put("authorities", authenticatedUserDetails.getAuthorities());
            response.put("message", "登录成功");
            response.put("timestamp", System.currentTimeMillis());

            System.out.println("=== 登录流程完成 ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== 认证失败 ===");
            System.out.println("异常类型: " + e.getClass().getSimpleName());
            System.out.println("异常消息: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "认证失败");
            response.put("message", "用户名或密码错误");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
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
            
            boolean isValid = jwtUtil.validateToken(request.getToken(), username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("username", username);
            response.put("userId", userId);
            response.put("companyId", companyId);
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

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities());
            response.put("enabled", userDetails.isEnabled());
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
     * 获取测试用户列表（开发阶段使用）
     */
    @GetMapping("/test-users")
    public ResponseEntity<Map<String, Object>> getTestUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("testUsers", userDetailsService.getTestUsers());
        response.put("message", "开发阶段测试用户列表");
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

            // 生成新的Token
            String newToken = jwtUtil.generateToken(username, 1L, 1L);
            
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
