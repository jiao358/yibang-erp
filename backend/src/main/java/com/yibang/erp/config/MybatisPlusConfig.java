package com.yibang.erp.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * MyBatis Plus 配置类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    @Primary
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));


        return interceptor;
    }

    /**
     * 自动填充处理器 - 修复Spring Boot 3.x兼容性问题
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }

    /**
     * 自定义MetaObjectHandler实现类 - 避免匿名内部类导致的类型推断问题
     */
    private static class CustomMetaObjectHandler implements MetaObjectHandler {
        
        @Override
        public void insertFill(MetaObject metaObject) {
            // 暂时注释掉，避免配置问题
            // this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
            // this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            // this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            // 暂时注释掉，避免配置问题
            // this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
