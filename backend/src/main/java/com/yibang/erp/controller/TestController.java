package com.yibang.erp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/health")
    public String health() {
        return "系统运行正常";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "欢迎使用亿邦智能供应链协作平台！";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from TestController!";
    }

    /**
     * 测试当前认证状态
     */
    @GetMapping("/auth-status")
    public Map<String, Object> getAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            response.put("authenticated", authentication.isAuthenticated());
            response.put("principal", authentication.getPrincipal());
            response.put("authorities", authentication.getAuthorities());
            response.put("name", authentication.getName());
            
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                response.put("username", userDetails.getUsername());
                response.put("enabled", userDetails.isEnabled());
                response.put("userAuthorities", userDetails.getAuthorities());
            }
        } else {
            response.put("authenticated", false);
            response.put("message", "没有认证信息");
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 测试受保护的资源访问
     */
    @GetMapping("/protected")
    public Map<String, Object> getProtectedResource() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是受保护的资源");
        response.put("timestamp", System.currentTimeMillis());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            response.put("user", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
        }
        
        return response;
    }
}
