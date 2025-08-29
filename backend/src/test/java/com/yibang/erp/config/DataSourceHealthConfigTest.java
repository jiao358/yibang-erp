package com.yibang.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据源健康检查测试类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@SpringBootTest
@ActiveProfiles("dev")
class DataSourceHealthConfigTest {

    @Autowired
    private DataSourceHealthConfig.DataSourceHealthChecker masterDataSourceHealthChecker;

    @Autowired
    private DataSourceHealthConfig.DataSourceHealthChecker slaveDataSourceHealthChecker;

    @Test
    void testMasterDataSourceHealthCheckerConfiguration() {
        assertNotNull(masterDataSourceHealthChecker, "主数据源健康检查器应该被正确配置");
        assertEquals("主数据源", masterDataSourceHealthChecker.getDataSourceName(), "主数据源名称应该正确");
    }

    @Test
    void testSlaveDataSourceHealthCheckerConfiguration() {
        assertNotNull(slaveDataSourceHealthChecker, "从数据源健康检查器应该被正确配置");
        assertEquals("从数据源", slaveDataSourceHealthChecker.getDataSourceName(), "从数据源名称应该正确");
    }

    @Test
    void testMasterDataSourceHealth() {
        try {
            boolean isHealthy = masterDataSourceHealthChecker.isHealthy();
            assertTrue(isHealthy, "主数据源应该是健康的");
            assertEquals("健康", masterDataSourceHealthChecker.getHealthStatus(), "主数据源状态应该是健康");
        } catch (Exception e) {
            fail("主数据源健康检查测试失败: " + e.getMessage());
        }
    }

    @Test
    void testSlaveDataSourceHealth() {
        try {
            boolean isHealthy = slaveDataSourceHealthChecker.isHealthy();
            assertTrue(isHealthy, "从数据源应该是健康的");
            assertEquals("健康", slaveDataSourceHealthChecker.getHealthStatus(), "从数据源状态应该是健康");
        } catch (Exception e) {
            fail("从数据源健康检查测试失败: " + e.getMessage());
        }
    }
}
