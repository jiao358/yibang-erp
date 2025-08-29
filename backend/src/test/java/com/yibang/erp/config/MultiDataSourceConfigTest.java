package com.yibang.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多数据源配置测试类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@SpringBootTest
@ActiveProfiles("dev")
class MultiDataSourceConfigTest {

    @Autowired
    private DataSource masterDataSource;

    @Autowired
    private DataSource slaveDataSource;

    @Autowired
    private DataSource dynamicDataSource;

    @Test
    void testMasterDataSourceConfiguration() {
        assertNotNull(masterDataSource, "主数据源应该被正确配置");
        assertTrue(masterDataSource instanceof com.zaxxer.hikari.HikariDataSource, "主数据源应该是HikariCP类型");
    }

    @Test
    void testSlaveDataSourceConfiguration() {
        assertNotNull(slaveDataSource, "从数据源应该被正确配置");
        assertTrue(slaveDataSource instanceof com.zaxxer.hikari.HikariDataSource, "从数据源应该是HikariCP类型");
    }

    @Test
    void testDynamicDataSourceConfiguration() {
        assertNotNull(dynamicDataSource, "动态数据源应该被正确配置");
        assertTrue(dynamicDataSource instanceof DynamicRoutingDataSource, "动态数据源应该是DynamicRoutingDataSource类型");
    }

    @Test
    void testDataSourceConnection() {
        try {
            // 测试主数据源连接
            JdbcTemplate masterJdbcTemplate = new JdbcTemplate(masterDataSource);
            Integer masterResult = masterJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            assertEquals(1, masterResult, "主数据源连接测试应该成功");

            // 测试从数据源连接
            JdbcTemplate slaveJdbcTemplate = new JdbcTemplate(slaveDataSource);
            Integer slaveResult = slaveJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            assertEquals(1, slaveResult, "从数据源连接测试应该成功");
        } catch (Exception e) {
            fail("数据源连接测试失败: " + e.getMessage());
        }
    }

    @Test
    void testDataSourceRouting() {
        try {
            // 测试数据源路由功能
            DynamicRoutingDataSource.setDataSourceType(MultiDataSourceConfig.DataSourceType.MASTER);
            assertEquals(MultiDataSourceConfig.DataSourceType.MASTER, DynamicRoutingDataSource.getDataSourceType());

            DynamicRoutingDataSource.setDataSourceType(MultiDataSourceConfig.DataSourceType.SLAVE);
            assertEquals(MultiDataSourceConfig.DataSourceType.SLAVE, DynamicRoutingDataSource.getDataSourceType());

            DynamicRoutingDataSource.clearDataSourceType();
            assertEquals(MultiDataSourceConfig.DataSourceType.MASTER, DynamicRoutingDataSource.getDataSourceType());
        } catch (Exception e) {
            fail("数据源路由测试失败: " + e.getMessage());
        }
    }
}
