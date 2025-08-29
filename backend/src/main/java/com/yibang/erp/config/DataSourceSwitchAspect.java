package com.yibang.erp.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * 数据源切换AOP切面 - 实现方法级别的数据源动态切换
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Aspect
@Order(1)
@Component
public class DataSourceSwitchAspect {

    /**
     * 定义切点 - 所有带有@DataSourceSwitch注解的方法
     */
    @Pointcut("@annotation(com.yibang.erp.config.DataSourceSwitch)")
    public void dataSourceSwitchPointcut() {}

    /**
     * 定义切点 - 所有带有@Transactional注解的方法
     */
    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void transactionalPointcut() {}

    /**
     * 环绕通知 - 处理数据源切换
     */
    @Around("dataSourceSwitchPointcut()")
    public Object aroundDataSourceSwitch(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取方法上的@DataSourceSwitch注解
        DataSourceSwitch dataSourceSwitch = AnnotationUtils.findAnnotation(method, DataSourceSwitch.class);
        if (dataSourceSwitch == null) {
            // 如果没有找到方法级别的注解，查找类级别的注解
            dataSourceSwitch = AnnotationUtils.findAnnotation(method.getDeclaringClass(), DataSourceSwitch.class);
        }
        
        if (dataSourceSwitch != null) {
            // 设置数据源类型
            MultiDataSourceConfig.DataSourceType dataSourceType = dataSourceSwitch.value();
            DynamicRoutingDataSource.setDataSourceType(dataSourceType);
            
            try {
                // 执行原方法
                return joinPoint.proceed();
            } finally {
                // 清除数据源类型设置
                DynamicRoutingDataSource.clearDataSourceType();
            }
        }
        
        // 如果没有注解，执行原方法
        return joinPoint.proceed();
    }

    /**
     * 环绕通知 - 处理事务方法的数据源选择
     */
    @Around("transactionalPointcut()")
    public Object aroundTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取方法上的@Transactional注解
        Transactional transactional = AnnotationUtils.findAnnotation(method, Transactional.class);
        if (transactional != null) {
            // 事务方法强制使用主数据源
            DynamicRoutingDataSource.setDataSourceType(MultiDataSourceConfig.DataSourceType.MASTER);
            
            try {
                // 执行原方法
                return joinPoint.proceed();
            } finally {
                // 清除数据源类型设置
                DynamicRoutingDataSource.clearDataSourceType();
            }
        }
        
        // 如果没有事务注解，执行原方法
        return joinPoint.proceed();
    }
}
