package com.yibang.erp.security.annotation;

import java.lang.annotation.*;

/**
 * 权限控制注解
 * 用于标记需要特定权限的方法或类
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    
    /**
     * 权限标识符
     * 格式：模块:操作，例如：user:create, product:read
     */
    String value();
    
    /**
     * 权限描述
     */
    String description() default "";
    
    /**
     * 权限类型
     */
    PermissionType type() default PermissionType.API;
    
    /**
     * 是否记录操作日志
     */
    boolean logOperation() default true;
    
    /**
     * 权限验证失败时的错误消息
     */
    String errorMessage() default "权限不足";
    
    /**
     * 权限类型枚举
     */
    enum PermissionType {
        /**
         * API权限
         */
        API,
        
        /**
         * 数据权限
         */
        DATA,
        
        /**
         * 功能权限
         */
        FUNCTION,
        
        /**
         * 菜单权限
         */
        MENU
    }
}
