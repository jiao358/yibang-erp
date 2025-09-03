package com.yibang.erp.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 服务状态数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStatusDTO {

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务状态
     */
    private String status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 版本号
     */
    private String version;

    /**
     * 运行时间
     */
    private String uptime;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 错误信息
     */
    private String errorMessage;
}
