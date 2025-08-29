package com.yibang.erp.security.annotation;

import java.lang.annotation.*;

/**
 * 数据范围注解
 * 用于控制用户可访问的数据范围
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    
    /**
     * 数据范围类型
     */
    ScopeType type() default ScopeType.COMPANY;
    
    /**
     * 关联的用户ID字段名
     * 默认为"createdBy"
     */
    String userField() default "createdBy";
    
    /**
     * 关联的公司ID字段名
     * 默认为"companyId"
     */
    String companyField() default "companyId";
    
    /**
     * 关联的部门ID字段名
     * 默认为"deptId"
     */
    String deptField() default "deptId";
    
    /**
     * 是否包含子部门数据
     */
    boolean includeSubDept() default false;
    
    /**
     * 是否包含子公司数据
     */
    boolean includeSubCompany() default false;
    
    /**
     * 自定义SQL条件
     * 支持SpEL表达式
     */
    String customCondition() default "";
    
    /**
     * 数据范围类型枚举
     */
    enum ScopeType {
        /**
         * 全部数据
         */
        ALL,
        
        /**
         * 公司数据
         */
        COMPANY,
        
        /**
         * 部门数据
         */
        DEPARTMENT,
        
        /**
         * 个人数据
         */
        PERSONAL,
        
        /**
         * 自定义数据范围
         */
        CUSTOM
    }
}
