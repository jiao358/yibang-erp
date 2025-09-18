package com.yibang.erp.controller.microservice;

import com.yibang.erp.domain.dto.microservice.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 时间服务Feign客户端
 * 用于调用yibang-taskmall系统的时间服务
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@FeignClient(name = "yibang-taskmall", path = "/api/hsf/time")
public interface TimeServiceClient {

    /**
     * 获取当前时间
     */
    @GetMapping("/current")
    Result<String> getCurrentTime(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取当前时间戳（毫秒）
     */
    @GetMapping("/timestamp")
    Result<Long> getCurrentTimestamp(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取当前时间戳（秒）
     */
    @GetMapping("/timestamp/seconds")
    Result<Long> getCurrentTimestampSeconds(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取格式化的当前时间
     */
    @GetMapping("/current/formatted")
    Result<String> getCurrentTimeFormatted(
            @RequestParam(defaultValue = "yyyy-MM-dd HH:mm:ss") String pattern,
            @RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取时间信息详情
     */
    @GetMapping("/info")
    Result<Map<String, Object>> getTimeInfo(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取当前日期
     */
    @GetMapping("/date")
    Result<String> getCurrentDate(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取当前时间（仅时间部分）
     */
    @GetMapping("/time-only")
    Result<String> getCurrentTimeOnly(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取当前周几
     */
    @GetMapping("/day-of-week")
    Result<Integer> getCurrentDayOfWeek(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取当前年份
     */
    @GetMapping("/year")
    Result<Integer> getCurrentYear(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取当前月份
     */
    @GetMapping("/month")
    Result<Integer> getCurrentMonth(@RequestHeader("X-API-Key") String apiKey);

    /**
     * 获取当前日期（几号）
     */
    @GetMapping("/day")
    Result<Integer> getCurrentDayOfMonth(@RequestHeader("X-API-Key") String apiKey);
}
