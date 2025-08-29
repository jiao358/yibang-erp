package com.yibang.erp.security.service.impl;

import com.yibang.erp.security.service.TenantService;
import com.yibang.erp.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 租户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    
    private static final ThreadLocal<String> TENANT_CONTEXT = new ThreadLocal<>();
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_PARAM = "tenantId";
    
    // 模拟租户配置数据，实际应用中应该从数据库或配置中心获取
    private static final Map<String, TenantConfig> TENANT_CONFIGS = new HashMap<>();
    
    static {
        // 初始化默认租户配置
        TENANT_CONFIGS.put("default", new TenantConfig(
            "default", "默认租户", "yibang_erp", "public", "localhost", true
        ));
        TENANT_CONFIGS.put("tenant1", new TenantConfig(
            "tenant1", "租户1", "yibang_erp_tenant1", "tenant1", "tenant1.localhost", true
        ));
        TENANT_CONFIGS.put("tenant2", new TenantConfig(
            "tenant2", "租户2", "yibang_erp_tenant2", "tenant2", "tenant2.localhost", true
        ));
    }
    
    @Override
    public String getCurrentTenantId() {
        String tenantId = TENANT_CONTEXT.get();
        if (tenantId != null) {
            return tenantId;
        }
        
        // 从请求头获取租户ID
        tenantId = getTenantIdFromHeader();
        if (tenantId != null) {
            setCurrentTenantId(tenantId);
            return tenantId;
        }
        
        // 从请求参数获取租户ID
        tenantId = getTenantIdFromParameter();
        if (tenantId != null) {
            setCurrentTenantId(tenantId);
            return tenantId;
        }
        
        // 从JWT Token获取租户ID
        tenantId = getTenantIdFromToken();
        if (tenantId != null) {
            setCurrentTenantId(tenantId);
            return tenantId;
        }
        
        // 从子域名获取租户ID
        tenantId = getTenantIdFromSubdomain();
        if (tenantId != null) {
            setCurrentTenantId(tenantId);
            return tenantId;
        }
        
        // 返回默认租户
        return "default";
    }
    
    @Override
    public void setCurrentTenantId(String tenantId) {
        if (tenantId != null && isTenantExists(tenantId)) {
            TENANT_CONTEXT.set(tenantId);
            log.debug("设置当前租户ID: {}", tenantId);
        } else {
            log.warn("无效的租户ID: {}", tenantId);
        }
    }
    
    @Override
    public void clearCurrentTenantId() {
        TENANT_CONTEXT.remove();
        log.debug("清除当前租户ID");
    }
    
    @Override
    public boolean isTenantExists(String tenantId) {
        return TENANT_CONFIGS.containsKey(tenantId);
    }
    
    @Override
    public TenantConfig getTenantConfig(String tenantId) {
        return TENANT_CONFIGS.get(tenantId);
    }
    
    @Override
    public boolean canAccessTenant(String tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return false;
        }
        
        // 检查租户是否存在
        if (!isTenantExists(tenantId)) {
            return false;
        }
        
        // 超级管理员可以访问所有租户
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return true;
        }
        
        // 检查用户是否有权限访问该租户
        // 这里可以根据实际业务需求实现更复杂的权限检查逻辑
        return true;
    }
    
    @Override
    public java.util.List<String> getUserAccessibleTenants(Long userId) {
        java.util.List<String> accessibleTenants = new java.util.ArrayList<>();
        
        // 超级管理员可以访问所有租户
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            accessibleTenants.addAll(TENANT_CONFIGS.keySet());
        } else {
            // 普通用户只能访问默认租户
            accessibleTenants.add("default");
        }
        
        return accessibleTenants;
    }
    
    @Override
    public void switchTenantContext(String tenantId) {
        if (isTenantExists(tenantId)) {
            setCurrentTenantId(tenantId);
            log.info("切换到租户: {}", tenantId);
        } else {
            log.warn("租户不存在，无法切换: {}", tenantId);
        }
    }
    
    /**
     * 从请求头获取租户ID
     */
    private String getTenantIdFromHeader() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader(TENANT_HEADER);
            }
        } catch (Exception e) {
            log.debug("从请求头获取租户ID失败", e);
        }
        return null;
    }
    
    /**
     * 从请求参数获取租户ID
     */
    private String getTenantIdFromParameter() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getParameter(TENANT_PARAM);
            }
        } catch (Exception e) {
            log.debug("从请求参数获取租户ID失败", e);
        }
        return null;
    }
    
    /**
     * 从JWT Token获取租户ID
     */
    private String getTenantIdFromToken() {
        try {
            // 这里应该从JWT Token中解析租户ID
            // 实际实现需要根据JWT Token的结构来解析
            return null;
        } catch (Exception e) {
            log.debug("从JWT Token获取租户ID失败", e);
        }
        return null;
    }
    
    /**
     * 从子域名获取租户ID
     */
    private String getTenantIdFromSubdomain() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String host = request.getServerName();
                
                // 解析子域名，例如：tenant1.localhost -> tenant1
                if (host.contains(".")) {
                    String subdomain = host.substring(0, host.indexOf("."));
                    if (isTenantExists(subdomain)) {
                        return subdomain;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("从子域名获取租户ID失败", e);
        }
        return null;
    }
}
