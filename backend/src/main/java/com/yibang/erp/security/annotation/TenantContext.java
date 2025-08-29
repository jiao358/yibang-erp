package com.yibang.erp.security.annotation;

import java.lang.annotation.*;

/**
 * 租户上下文注解
 * 用于标记多租户相关的配置和方法
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantContext {
    
    /**
     * 租户标识字段名
     * 默认为"tenantId"
     */
    String tenantField() default "tenantId";
    
    /**
     * 是否自动注入租户ID
     */
    boolean autoInject() default true;
    
    /**
     * 租户ID来源
     */
    TenantSource source() default TenantSource.HEADER;
    
    /**
     * 租户ID来源枚举
     */
    enum TenantSource {
        /**
         * 从请求头获取
         */
        HEADER,
        
        /**
         * 从请求参数获取
         */
        PARAMETER,
        
        /**
         * 从JWT Token获取
         */
        TOKEN,
        
        /**
         * 从数据库获取
         */
        DATABASE,
        
        /**
         * 从子域名获取
         */
        SUBDOMAIN
    }
}
