package com.yibang.erp.config;

import com.yibang.erp.security.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置拦截器、跨域等
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final PermissionInterceptor permissionInterceptor;
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
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
                "/v3/api-docs/**",     // 排除API文档
                "/static/**"           // 排除静态资源访问
            )
            .order(1); // 设置拦截器顺序
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问
        String resourcePath = uploadPath.startsWith("./") ? 
            System.getProperty("user.dir") + "/" + uploadPath.substring(2) : uploadPath;
        
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:" + resourcePath)
                .setCachePeriod(3600); // 缓存1小时
    }
}
