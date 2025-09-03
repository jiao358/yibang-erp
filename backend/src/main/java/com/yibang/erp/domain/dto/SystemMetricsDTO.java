package com.yibang.erp.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统指标数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsDTO {

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 系统基本信息
     */
    private SystemInfo systemInfo;

    /**
     * JVM指标
     */
    private JVMMetrics jvmMetrics;

    /**
     * 数据库状态
     */
    private DatabaseStatus databaseStatus;

    /**
     * 服务状态列表
     */
    private List<ServiceStatus> services;

    /**
     * 告警信息列表
     */
    private List<AlertInfo> alerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInfo {
        private String osName;
        private String osVersion;
        private String javaVersion;
        private String javaVendor;
        private LocalDateTime startTime;
        private Long uptime; // 运行时间（毫秒）
        private Integer processorCount;
        private String hostname;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JVMMetrics {
        private Long totalMemory; // 总内存（字节）
        private Long freeMemory;  // 空闲内存（字节）
        private Long usedMemory;  // 已用内存（字节）
        private Double memoryUsagePercentage; // 内存使用率
        private Integer threadCount; // 线程数
        private Integer peakThreadCount; // 峰值线程数
        private Long totalStartedThreadCount; // 总启动线程数
        private Double cpuUsage; // CPU使用率
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseStatus {
        private String status; // 数据库状态
        private Integer activeConnections; // 活跃连接数
        private Integer totalConnections; // 总连接数
        private Integer idleConnections; // 空闲连接数
        private Integer maxConnections; // 最大连接数
        private Double connectionUsagePercentage; // 连接使用率
        private Long avgResponseTime; // 平均响应时间（毫秒）
        private Integer errorCount; // 错误连接数
        private Integer timeoutCount; // 超时连接数
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStatus {
        private String name; // 服务名称
        private String status; // 服务状态
        private String statusText; // 状态文本
        private Integer port; // 端口号
        private String version; // 版本号
        private String uptime; // 运行时间
        private LocalDateTime lastCheckTime; // 最后检查时间
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertInfo {
        private Long id;
        private String level; // 告警级别：info, warning, error
        private String title; // 告警标题
        private String message; // 告警消息
        private LocalDateTime time; // 告警时间
        private String status; // 告警状态：active, acknowledged, resolved
    }
}
