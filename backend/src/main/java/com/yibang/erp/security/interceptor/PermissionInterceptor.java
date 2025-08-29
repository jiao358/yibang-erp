package com.yibang.erp.security.interceptor;

import com.yibang.erp.security.annotation.RequiresPermission;
import com.yibang.erp.security.annotation.DataScope;
import com.yibang.erp.security.service.PermissionService;
import com.yibang.erp.security.service.DataScopeService;
import com.yibang.erp.common.exception.PermissionDeniedException;
import com.yibang.erp.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 权限拦截器
 * 负责拦截请求并验证用户权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    
    private final PermissionService permissionService;
    private final DataScopeService dataScopeService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();
        
        // 检查类级别的权限注解
        RequiresPermission classPermission = clazz.getAnnotation(RequiresPermission.class);
        if (classPermission != null) {
            if (!checkPermission(classPermission)) {
                throw new PermissionDeniedException(classPermission.errorMessage());
            }
        }
        
        // 检查方法级别的权限注解
        RequiresPermission methodPermission = method.getAnnotation(RequiresPermission.class);
        if (methodPermission != null) {
            if (!checkPermission(methodPermission)) {
                throw new PermissionDeniedException(methodPermission.errorMessage());
            }
        }
        
        // 检查数据范围注解
        DataScope dataScope = method.getAnnotation(DataScope.class);
        if (dataScope != null) {
            dataScopeService.setDataScope(dataScope);
        }
        
        return true;
    }
    
    /**
     * 检查权限
     */
    private boolean checkPermission(RequiresPermission permission) {
        try {
            String currentUsername = SecurityUtils.getCurrentUsername();
            if (currentUsername == null) {
                log.warn("用户未登录，无法验证权限: {}", permission.value());
                return false;
            }
            
            boolean hasPermission = permissionService.hasPermission(currentUsername, permission.value());
            
            if (permission.logOperation()) {
                log.info("权限验证 - 用户: {}, 权限: {}, 结果: {}", 
                    currentUsername, permission.value(), hasPermission);
            }
            
            return hasPermission;
        } catch (Exception e) {
            log.error("权限验证失败: {}", permission.value(), e);
            return false;
        }
    }
}
