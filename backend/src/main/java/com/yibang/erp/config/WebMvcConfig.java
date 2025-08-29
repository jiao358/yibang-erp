package com.yibang.erp.config;

import com.yibang.erp.security.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置拦截器、跨域等
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final PermissionInterceptor permissionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册权限拦截器
        registry.addInterceptor(permissionInterceptor)
            .addPathPatterns("/api/**") // 拦截所有API请求
            .excludePathPatterns(
                "/api/auth/**",        // 排除认证相关接口
                "/api/public/**",      // 排除公开接口
                "/error",              // 排除错误页面
                "/swagger-ui/**",      // 排除Swagger UI
                "/v3/api-docs/**"      // 排除API文档
            )
            .order(1); // 设置拦截器顺序
    }
}
