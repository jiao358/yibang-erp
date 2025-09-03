package com.yibang.erp.domain.service.impl;

import com.yibang.erp.domain.dto.SystemMetricsDTO;
import com.yibang.erp.domain.dto.DatabaseStatusDTO;
import com.yibang.erp.domain.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统监控服务实现类
 * 实现按需采集的监控数据获取
 */
@Service
@Slf4j
public class MonitorServiceImpl implements MonitorService {

    private SystemMetricsDTO latestData;

    @Override
    public SystemMetricsDTO collectSystemInfo() {
        try {
            log.info("开始采集系统基本信息");
            
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();
            
            SystemMetricsDTO.SystemInfo systemInfo = SystemMetricsDTO.SystemInfo.builder()
                .osName(osBean.getName())
                .osVersion(osBean.getVersion())
                .javaVersion(System.getProperty("java.version"))
                .javaVendor(System.getProperty("java.vendor"))
                .startTime(LocalDateTime.now().minus(Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime())))
                .uptime(ManagementFactory.getRuntimeMXBean().getUptime())
                .processorCount(osBean.getAvailableProcessors())
                .hostname(System.getenv("HOSTNAME") != null ? System.getenv("HOSTNAME") : "unknown")
                .build();

            // 创建基本的系统指标数据
            SystemMetricsDTO result = SystemMetricsDTO.builder()
                .collectTime(LocalDateTime.now())
                .systemInfo(systemInfo)
                .build();

            // 缓存最新数据
            this.latestData = result;
            
            log.info("系统基本信息采集完成");
            return result;
            
        } catch (Exception e) {
            log.error("采集系统基本信息失败", e);
            throw new RuntimeException("采集系统基本信息失败", e);
        }
    }

    @Override
    public SystemMetricsDTO collectJVMMetrics() {
        try {
            log.info("开始采集JVM指标");
            
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            Runtime runtime = Runtime.getRuntime();
            
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercentage = (double) usedMemory / totalMemory * 100;
            
            SystemMetricsDTO.JVMMetrics jvmMetrics = SystemMetricsDTO.JVMMetrics.builder()
                .totalMemory(totalMemory)
                .freeMemory(freeMemory)
                .usedMemory(usedMemory)
                .memoryUsagePercentage(memoryUsagePercentage)
                .threadCount(threadBean.getThreadCount())
                .peakThreadCount(threadBean.getPeakThreadCount())
                .totalStartedThreadCount(threadBean.getTotalStartedThreadCount())
                .cpuUsage(0.0) // 暂时设为0，后续可以通过其他方式获取
                .build();

            // 创建JVM指标数据
            SystemMetricsDTO result = SystemMetricsDTO.builder()
                .collectTime(LocalDateTime.now())
                .jvmMetrics(jvmMetrics)
                .build();

            // 缓存最新数据
            this.latestData = result;
            
            log.info("JVM指标采集完成");
            return result;
            
        } catch (Exception e) {
            log.error("采集JVM指标失败", e);
            throw new RuntimeException("采集JVM指标失败", e);
        }
    }

    @Override
    public DatabaseStatusDTO collectDatabaseStatus() {
        try {
            log.info("开始采集数据库状态");
            
            // 这里暂时返回模拟数据，后续可以集成真实的数据库连接池监控
            DatabaseStatusDTO dbStatus = DatabaseStatusDTO.builder()
                .collectTime(LocalDateTime.now())
                .status("正常")
                .activeConnections(5)
                .totalConnections(10)
                .idleConnections(5)
                .maxConnections(20)
                .connectionUsagePercentage(50.0)
                .avgResponseTime(15L)
                .errorCount(0)
                .timeoutCount(0)
                .version("MySQL 8.0")
                .databaseName("yibang_erp")
                .lastCheckTime(LocalDateTime.now())
                .build();
            
            log.info("数据库状态采集完成");
            return dbStatus;
            
        } catch (Exception e) {
            log.error("采集数据库状态失败", e);
            throw new RuntimeException("采集数据库状态失败", e);
        }
    }

    @Override
    public SystemMetricsDTO getLatestData() {
        if (latestData == null) {
            log.info("没有缓存数据，开始采集系统信息");
            return collectSystemInfo();
        }
        return latestData;
    }

    @Override
    public SystemMetricsDTO collectAllMetrics() {
        try {
            log.info("开始采集所有监控数据");
            
            // 采集系统信息
            SystemMetricsDTO.SystemInfo systemInfo = collectSystemInfo().getSystemInfo();
            
            // 采集JVM指标
            SystemMetricsDTO.JVMMetrics jvmMetrics = collectJVMMetrics().getJvmMetrics();
            
            // 采集数据库状态
            DatabaseStatusDTO dbStatus = collectDatabaseStatus();
            
            // 创建服务状态列表
            List<SystemMetricsDTO.ServiceStatus> services = createServiceStatusList();
            
            // 创建告警信息列表
            List<SystemMetricsDTO.AlertInfo> alerts = createAlertList();
            
            // 构建完整的监控数据
            SystemMetricsDTO result = SystemMetricsDTO.builder()
                .collectTime(LocalDateTime.now())
                .systemInfo(systemInfo)
                .jvmMetrics(jvmMetrics)
                .databaseStatus(convertToSystemMetricsDatabaseStatus(dbStatus))
                .services(services)
                .alerts(alerts)
                .build();

            // 缓存最新数据
            this.latestData = result;
            
            log.info("所有监控数据采集完成");
            return result;
            
        } catch (Exception e) {
            log.error("采集所有监控数据失败", e);
            throw new RuntimeException("采集所有监控数据失败", e);
        }
    }

    /**
     * 创建服务状态列表
     */
    private List<SystemMetricsDTO.ServiceStatus> createServiceStatusList() {
        List<SystemMetricsDTO.ServiceStatus> services = new ArrayList<>();
        
        services.add(SystemMetricsDTO.ServiceStatus.builder()
            .name("Web服务")
            .status("success")
            .statusText("运行中")
            .port(7102)
            .version("v1.0.0")
            .uptime("运行中")
            .lastCheckTime(LocalDateTime.now())
            .build());
            
        services.add(SystemMetricsDTO.ServiceStatus.builder()
            .name("数据库服务")
            .status("success")
            .statusText("运行中")
            .port(3306)
            .version("MySQL 8.0")
            .uptime("运行中")
            .lastCheckTime(LocalDateTime.now())
            .build());
            
        services.add(SystemMetricsDTO.ServiceStatus.builder()
            .name("Redis服务")
            .status("success")
            .statusText("运行中")
            .port(6379)
            .version("v7.0")
            .uptime("运行中")
            .lastCheckTime(LocalDateTime.now())
            .build());
            
        return services;
    }

    /**
     * 创建告警信息列表
     */
    private List<SystemMetricsDTO.AlertInfo> createAlertList() {
        List<SystemMetricsDTO.AlertInfo> alerts = new ArrayList<>();
        
        // 根据实际系统状态生成告警
        if (latestData != null && latestData.getJvmMetrics() != null) {
            double memoryUsage = latestData.getJvmMetrics().getMemoryUsagePercentage();
            if (memoryUsage > 80) {
                alerts.add(SystemMetricsDTO.AlertInfo.builder()
                    .id(1L)
                    .level("warning")
                    .title("内存使用率过高")
                    .message("系统内存使用率已达到" + String.format("%.1f", memoryUsage) + "%，建议及时清理或扩容")
                    .time(LocalDateTime.now())
                    .status("active")
                    .build());
            }
        }
        
        return alerts;
    }

    /**
     * 将DatabaseStatusDTO转换为SystemMetricsDTO中的DatabaseStatus
     */
    private SystemMetricsDTO.DatabaseStatus convertToSystemMetricsDatabaseStatus(DatabaseStatusDTO dbStatus) {
        return SystemMetricsDTO.DatabaseStatus.builder()
            .status(dbStatus.getStatus())
            .activeConnections(dbStatus.getActiveConnections())
            .totalConnections(dbStatus.getTotalConnections())
            .idleConnections(dbStatus.getIdleConnections())
            .maxConnections(dbStatus.getMaxConnections())
            .connectionUsagePercentage(dbStatus.getConnectionUsagePercentage())
            .avgResponseTime(dbStatus.getAvgResponseTime())
            .errorCount(dbStatus.getErrorCount())
            .timeoutCount(dbStatus.getTimeoutCount())
            .build();
    }
}
