package com.yibang.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 基础配置类
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        System.out.println("=== 配置认证提供者 ===");
        System.out.println("UserDetailsService类型: " + userDetailsService.getClass().getSimpleName());
        System.out.println("PasswordEncoder类型: " + passwordEncoder.getClass().getSimpleName());
        
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        
        System.out.println("认证提供者配置完成");
        System.out.println("=====================");
        
        return provider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤器链配置 - 优化版本
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        System.out.println("=== 配置安全过滤器链 ===");
        
        http
            // 禁用CSRF（使用JWT认证）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理（无状态）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置认证提供者
            .authenticationProvider(authenticationProvider)
            
            // 配置授权规则 - 优化版本
            .authorizeHttpRequests(authz -> authz
                // 公开接口
                .requestMatchers("/").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // 需要认证的接口（排除已公开的）
                .requestMatchers("/api/protected/**").authenticated()
                .requestMatchers("/api/admin/**").authenticated()
                
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            
            // 配置HTTP Basic认证（开发阶段临时使用）
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // 配置表单登录（开发阶段临时使用）
            .formLogin(AbstractHttpConfigurer::disable);

        System.out.println("=== 安全过滤器链配置完成 ===");
        return http.build();
    }

    /**
     * CORS配置 - 优化版本
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源 - 开发环境
        List<String> allowedOrigins = Arrays.asList(
            "http://localhost:3000",      // React开发服务器
            "http://localhost:8080",      // 本地后端
            "http://localhost:5173",      // Vite开发服务器
            "http://127.0.0.1:3000",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:5173"
        );
        configuration.setAllowedOrigins(allowedOrigins);
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Origin", "Content-Type", "Accept", "Authorization", 
            "X-Requested-With", "Cache-Control", "X-File-Name"
        ));
        
        // 允许发送凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的有效期
        configuration.setMaxAge(3600L);
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
