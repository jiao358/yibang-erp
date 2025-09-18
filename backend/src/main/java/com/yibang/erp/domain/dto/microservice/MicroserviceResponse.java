package com.yibang.erp.domain.dto.microservice;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微服务统一响应格式
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class MicroserviceResponse<T> {
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 请求追踪ID
     */
    private String traceId;
    
    public MicroserviceResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public MicroserviceResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public static <T> MicroserviceResponse<T> success(T data) {
        return new MicroserviceResponse<>(200, "操作成功", data);
    }
    
    public static <T> MicroserviceResponse<T> success(String message, T data) {
        return new MicroserviceResponse<>(200, message, data);
    }
    
    public static <T> MicroserviceResponse<T> error(Integer code, String message) {
        return new MicroserviceResponse<>(code, message, null);
    }
    
    public static <T> MicroserviceResponse<T> error(String message) {
        return new MicroserviceResponse<>(500, message, null);
    }
    
    public static <T> MicroserviceResponse<T> badRequest(String message) {
        return new MicroserviceResponse<>(400, message, null);
    }
    
    public static <T> MicroserviceResponse<T> unauthorized(String message) {
        return new MicroserviceResponse<>(401, message, null);
    }
    
    public static <T> MicroserviceResponse<T> forbidden(String message) {
        return new MicroserviceResponse<>(403, message, null);
    }
    
    public static <T> MicroserviceResponse<T> notFound(String message) {
        return new MicroserviceResponse<>(404, message, null);
    }
}
