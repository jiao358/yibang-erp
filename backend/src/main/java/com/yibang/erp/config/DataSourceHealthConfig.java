package com.yibang.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据源健康检查配置 - 暂时禁用，避免与原有配置冲突
 * 等Redis配置测试通过后再启用
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Configuration
public class DataSourceHealthConfig {

    // 暂时注释掉所有Bean，避免冲突
    /*
    @Bean
    public DataSourceHealthChecker masterDataSourceHealthChecker(DataSource masterDataSource) {
        return new DataSourceHealthChecker(masterDataSource, "主数据源");
    }

    @Bean
    public DataSourceHealthChecker slaveDataSourceHealthChecker(DataSource slaveDataSource) {
        return new DataSourceHealthChecker(slaveDataSource, "从数据源");
    }
    */

    /**
     * 数据源健康检查实现
     */
    public static class DataSourceHealthChecker {
        private final DataSource dataSource;
        private final String dataSourceName;

        public DataSourceHealthChecker(DataSource dataSource, String dataSourceName) {
            this.dataSource = dataSource;
            this.dataSourceName = dataSourceName;
        }

        /**
         * 检查数据源健康状态
         */
        public boolean isHealthy() {
            try (Connection connection = dataSource.getConnection()) {
                // 测试连接是否正常
                if (connection.isValid(5)) {
                    // 执行简单查询测试
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                    Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                    return result != null && result == 1;
                }
                return false;
            } catch (SQLException e) {
                return false;
            }
        }

        /**
         * 获取数据源名称
         */
        public String getDataSourceName() {
            return dataSourceName;
        }

        /**
         * 获取健康状态描述
         */
        public String getHealthStatus() {
            return isHealthy() ? "健康" : "异常";
        }
    }
}
