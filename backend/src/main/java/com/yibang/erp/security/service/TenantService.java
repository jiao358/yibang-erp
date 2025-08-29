package com.yibang.erp.security.service;

import com.yibang.erp.security.annotation.TenantContext;

/**
 * 租户服务接口
 * 负责多租户隔离管理
 */
public interface TenantService {
    
    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    String getCurrentTenantId();
    
    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    void setCurrentTenantId(String tenantId);
    
    /**
     * 清除当前租户ID
     */
    void clearCurrentTenantId();
    
    /**
     * 检查租户是否存在
     *
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean isTenantExists(String tenantId);
    
    /**
     * 获取租户配置
     *
     * @param tenantId 租户ID
     * @return 租户配置
     */
    TenantConfig getTenantConfig(String tenantId);
    
    /**
     * 验证租户访问权限
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean canAccessTenant(String tenantId, Long userId);
    
    /**
     * 获取用户可访问的租户列表
     *
     * @param userId 用户ID
     * @return 租户ID列表
     */
    java.util.List<String> getUserAccessibleTenants(Long userId);
    
    /**
     * 切换租户上下文
     *
     * @param tenantId 租户ID
     */
    void switchTenantContext(String tenantId);
    
    /**
     * 租户配置类
     */
    class TenantConfig {
        private String tenantId;
        private String tenantName;
        private String databaseName;
        private String schemaName;
        private String domain;
        private Boolean isActive;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        
        // 构造函数
        public TenantConfig() {}
        
        public TenantConfig(String tenantId, String tenantName, String databaseName, String schemaName, 
                           String domain, Boolean isActive) {
            this.tenantId = tenantId;
            this.tenantName = tenantName;
            this.databaseName = databaseName;
            this.schemaName = schemaName;
            this.domain = domain;
            this.isActive = isActive;
        }
        
        // Getter和Setter方法
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getTenantName() { return tenantName; }
        public void setTenantName(String tenantName) { this.tenantName = tenantName; }
        
        public String getDatabaseName() { return databaseName; }
        public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
        
        public String getSchemaName() { return schemaName; }
        public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
        
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}
