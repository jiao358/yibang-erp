package com.yibang.erp.security;

import com.yibang.erp.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 添加请求信息日志
        logger.info("JWT认证过滤器 - 处理请求: {} {}", request.getMethod(), request.getRequestURI());
        
        try {
            String jwt = getJwtFromRequest(request);
            logger.info("JWT认证过滤器 - 提取的JWT: {}", jwt != null ? jwt.substring(0, Math.min(50, jwt.length())) + "..." : "null");

            if (jwt != null && !jwt.isEmpty()) {
                String username = jwtUtil.getUsernameFromToken(jwt);
                logger.info("JWT认证过滤器 - 从token提取的用户名: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 从token中提取角色信息
                    List<String> roles = jwtUtil.getRolesFromToken(jwt);
                    logger.info("JWT认证过滤器 - 用户名: {}, 提取的角色: {}", username, roles);
                    
                    if (roles != null && !roles.isEmpty()) {
                        // 创建权限列表 - 修复角色前缀问题
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> {
                                // 如果角色已经包含ROLE_前缀，则不重复添加
                                if (role.startsWith("ROLE_")) {
                                    return new SimpleGrantedAuthority(role);
                                } else {
                                    return new SimpleGrantedAuthority("ROLE_" + role);
                                }
                            })
                            .collect(Collectors.toList());
                        
                        logger.info("JWT认证过滤器 - 创建的权限: {}", authorities);
                        
                        if (jwtUtil.validateToken(jwt, username)) {
                            // 创建包含用户详情的认证对象
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(
                                    userDetails, null, authorities);
                            
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.info("JWT认证过滤器 - 认证成功，设置权限: {}", authorities);
                        } else {
                            logger.warn("JWT认证过滤器 - Token验证失败");
                        }
                    } else {
                        logger.warn("JWT认证过滤器 - 未找到角色信息，回退到数据库查询");
                        // 如果token中没有角色信息，回退到原来的方式
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtUtil.validateToken(jwt, username)) {
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.info("JWT认证过滤器 - 回退认证成功，权限: {}", userDetails.getAuthorities());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("无法设置用户认证: {}", e.getMessage(), e);
        }

        // 添加认证结果日志
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.info("JWT认证过滤器 - 当前认证状态: 已认证, 用户: {}, 权限: {}", 
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        } else {
            logger.info("JWT认证过滤器 - 当前认证状态: 未认证");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
