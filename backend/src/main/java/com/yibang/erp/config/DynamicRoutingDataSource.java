package com.yibang.erp.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态数据源路由配置 - 实现读写分离
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<MultiDataSourceConfig.DataSourceType> contextHolder = new ThreadLocal<>();

    @Override
    protected Object determineCurrentLookupKey() {
        return getDataSourceType();
    }

    /**
     * 设置当前线程的数据源类型
     */
    public static void setDataSourceType(MultiDataSourceConfig.DataSourceType dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    /**
     * 获取当前线程的数据源类型
     */
    public static MultiDataSourceConfig.DataSourceType getDataSourceType() {
        MultiDataSourceConfig.DataSourceType dataSourceType = contextHolder.get();
        return dataSourceType != null ? dataSourceType : MultiDataSourceConfig.DataSourceType.MASTER;
    }

    /**
     * 清除当前线程的数据源类型
     */
    public static void clearDataSourceType() {
        contextHolder.remove();
    }

    /**
     * 设置目标数据源
     */
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    /**
     * 设置默认数据源
     */
    public void setDefaultTargetDataSource(DataSource defaultTargetDataSource) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
    }
}
