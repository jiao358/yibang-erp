package com.yibang.erp.config;

import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.security.HsfApiKeyFilter;
import com.yibang.erp.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

//    @Autowired
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//    @Autowired
//    private HsfApiKeyFilter hsfApiKeyFilter;

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
     * JWT认证过滤器
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    /**
     * HSF API Key认证过滤器
     */
    @Bean
    public HsfApiKeyFilter hsfApiKeyFilter() {
        return new HsfApiKeyFilter();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,JwtAuthenticationFilter jwtAuthenticationFilter,HsfApiKeyFilter hsfApiKeyFilter) throws Exception {
        System.out.println("=== 配置安全过滤器链 ===");
        
        http
            // 禁用CSRF，因为使用JWT
            .csrf(csrf -> csrf.disable())
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 配置会话管理
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/health",
                    "/api/auth/login",
                    "/api/auth/**",
                    "/api/test/**",
                    "/actuator/**",
                    "/static/**",
                    "/api/hsf/**"  // HSF微服务接口 - 通过API Key认证
                ).permitAll()
                .requestMatchers("/api/monitor/**").hasRole("SYSTEM_ADMIN")
                .requestMatchers("/api/protected/**").authenticated()
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            // 添加HSF API Key认证过滤器（在JWT过滤器之前）
            .addFilterBefore(hsfApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
            "http://localhost:7101",      // 前端Vite开发服务器
            "http://localhost:8080",      // 本地后端
            "http://localhost:5173",      // Vite开发服务器
            "http://127.0.0.1:3000",
            "http://127.0.0.1:7101",
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
