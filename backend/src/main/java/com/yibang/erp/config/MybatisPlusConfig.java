package com.yibang.erp.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件 - 简化配置，避免Spring Boot 3.x兼容性问题
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // PaginationInnerInterceptor 在新版本中已被移除，需要自定义分页逻辑
        // 这里可以添加其他内置拦截器，如乐观锁等
        return interceptor;
    }

    /**
     * 自动填充处理器 - 简化配置，避免类型推断问题
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }

    /**
     * 自定义MetaObjectHandler实现类
     */
    private static class CustomMetaObjectHandler implements MetaObjectHandler {
        
        @Override
        public void insertFill(MetaObject metaObject) {
            // 暂时注释掉，避免配置问题
             this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
             this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
             this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            // 暂时注释掉，避免配置问题
             this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
