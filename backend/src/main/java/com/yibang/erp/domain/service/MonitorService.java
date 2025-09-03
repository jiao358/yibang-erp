package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.SystemMetricsDTO;
import com.yibang.erp.domain.dto.DatabaseStatusDTO;

/**
 * 系统监控服务接口
 * 实现按需采集的监控数据获取
 */
public interface MonitorService {

    /**
     * 采集系统基本信息
     * @return 系统指标数据
     */
    SystemMetricsDTO collectSystemInfo();

    /**
     * 采集JVM指标
     * @return JVM指标数据
     */
    SystemMetricsDTO collectJVMMetrics();

    /**
     * 采集数据库状态
     * @return 数据库状态数据
     */
    DatabaseStatusDTO collectDatabaseStatus();

    /**
     * 获取最新采集的数据
     * @return 最新的监控数据
     */
    SystemMetricsDTO getLatestData();

    /**
     * 采集所有监控数据
     * @return 完整的系统监控数据
     */
    SystemMetricsDTO collectAllMetrics();
}
