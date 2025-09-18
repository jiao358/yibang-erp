package com.yibang.erp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * HSF API Key认证过滤器
 * 验证内部系统API Key
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Component
public class HsfApiKeyFilter implements Filter {

    @Value("${hsf.security.api-keys:yibang-erp:erp-2024-abc123def456,other-system:other-2024-xyz789}")
    private String apiKeysConfig;

    private List<String> validApiKeys;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 只对HSF API路径进行API Key验证
        if (httpRequest.getRequestURI().startsWith("/api/hsf/")) {

            
            String apiKey = httpRequest.getHeader("X-API-Key");
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.warn("HSF API调用缺少API Key: {}", httpRequest.getRequestURI());
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"code\":401,\"message\":\"缺少API Key\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
                return;
            }

            if (!validApiKeys.contains(apiKey)) {
                log.warn("HSF API调用使用了无效的API Key: {} from {}", apiKey, httpRequest.getRemoteAddr());
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"code\":401,\"message\":\"无效的API Key\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
                return;
            }
            String systemName = validateApiKey(apiKey);
            if (systemName == null) {
                log.warn("无效的 API Key: {} from {}", apiKey, getClientIp(httpRequest));
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"code\":401,\"message\":\"无效的 API Key\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
                return;
            }

            // 设置认证状态，跳过 JWT 认证
            setAuthentication(systemName, apiKey);

            log.debug("HSF API调用验证通过: {} from {}", apiKey, httpRequest.getRemoteAddr());
        }
        //对jwt验证对象写入
        
        chain.doFilter(request, response);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 验证 API Key
     */
    private String validateApiKey(String apiKey) {
       if(validApiKeys.contains(apiKey)){
           return apiKey;
       }
        return null;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 解析配置的 API Keys

        // 懒加载API Keys
        if (validApiKeys == null) {
            validApiKeys = Arrays.stream(apiKeysConfig.split(","))
                    .map(entry -> entry.split(":")[1]) // 取key部分
                    .toList();
        }

        log.info("API Key 认证过滤器初始化完成，已配置 {} 个内部系统", validApiKeys.size());
    }

    /**
     * 解析 API Keys 配置
     */
    private Map<String, String> parseApiKeys(String config) {
        Map<String, String> apiKeys = new java.util.HashMap<>();

        if (config == null || config.trim().isEmpty()) {
            return apiKeys;
        }

        // 解析格式: system1:key1,system2:key2
        String[] pairs = config.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split(":");
            if (keyValue.length == 2) {
                apiKeys.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return apiKeys;
    }



    /**
     * 设置认证状态
     */
    private void setAuthentication(String systemName, String apiKey) {
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"),
                new SimpleGrantedAuthority("ROLE_HSF_ACCESS")
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(systemName, apiKey, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}
