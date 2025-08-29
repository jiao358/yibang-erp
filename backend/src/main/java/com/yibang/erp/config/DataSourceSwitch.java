package com.yibang.erp.config;

import java.lang.annotation.*;

/**
 * 数据源切换注解 - 用于在方法级别控制数据源选择
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSourceSwitch {
    
    /**
     * 数据源类型
     */
    MultiDataSourceConfig.DataSourceType value() default MultiDataSourceConfig.DataSourceType.MASTER;
    
    /**
     * 是否强制使用指定数据源（忽略事务上下文）
     */
    boolean force() default false;
}
