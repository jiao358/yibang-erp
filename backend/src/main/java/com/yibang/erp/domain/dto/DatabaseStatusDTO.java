package com.yibang.erp.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据库状态数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseStatusDTO {

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 数据库状态
     */
    private String status;

    /**
     * 活跃连接数
     */
    private Integer activeConnections;

    /**
     * 总连接数
     */
    private Integer totalConnections;

    /**
     * 空闲连接数
     */
    private Integer idleConnections;

    /**
     * 最大连接数
     */
    private Integer maxConnections;

    /**
     * 连接使用率
     */
    private Double connectionUsagePercentage;

    /**
     * 平均响应时间（毫秒）
     */
    private Long avgResponseTime;

    /**
     * 错误连接数
     */
    private Integer errorCount;

    /**
     * 超时连接数
     */
    private Integer timeoutCount;

    /**
     * 数据库版本
     */
    private String version;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;
}
