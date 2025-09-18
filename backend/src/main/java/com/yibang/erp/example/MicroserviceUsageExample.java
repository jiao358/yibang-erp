package com.yibang.erp.example;

import com.yibang.erp.controller.microservice.TimeServiceClient;
import com.yibang.erp.domain.dto.microservice.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微服务使用示例
 * 展示如何在其他服务中使用Feign客户端调用微服务
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Component
public class MicroserviceUsageExample {

    @Autowired
    private TimeServiceClient timeServiceClient;
    
    @Value("${hsf.api.key}")
    private String hsfApiKey;

    /**
     * 调用外部时间服务示例
     */
    public void timeServiceExample() {
        log.info("=== 调用外部时间服务示例 ===");
        
        try {
            // 1. 获取当前时间
            Result<String> currentTimeResult = timeServiceClient.getCurrentTime(hsfApiKey);
            if (currentTimeResult.isSuccess()) {
                log.info("当前时间: {}", currentTimeResult.getData());
            } else {
                log.error("获取当前时间失败: {}", currentTimeResult.getMessage());
            }
            
            // 2. 获取时间戳
            Result<Long> timestampResult = timeServiceClient.getCurrentTimestamp(hsfApiKey);
            if (timestampResult.isSuccess()) {
                log.info("当前时间戳: {}", timestampResult.getData());
            } else {
                log.error("获取时间戳失败: {}", timestampResult.getMessage());
            }
            
            // 3. 获取格式化时间
            Result<String> formattedTimeResult = timeServiceClient.getCurrentTimeFormatted("yyyy年MM月dd日 HH:mm:ss", hsfApiKey);
            if (formattedTimeResult.isSuccess()) {
                log.info("格式化时间: {}", formattedTimeResult.getData());
            } else {
                log.error("获取格式化时间失败: {}", formattedTimeResult.getMessage());
            }
            
            // 4. 获取时间详情
            Result<Map<String, Object>> timeInfoResult = timeServiceClient.getTimeInfo(hsfApiKey);
            if (timeInfoResult.isSuccess()) {
                log.info("时间详情: {}", timeInfoResult.getData());
            } else {
                log.error("获取时间详情失败: {}", timeInfoResult.getMessage());
            }
            
            // 5. 获取当前日期
            Result<String> currentDateResult = timeServiceClient.getCurrentDate(hsfApiKey);
            if (currentDateResult.isSuccess()) {
                log.info("当前日期: {}", currentDateResult.getData());
            } else {
                log.error("获取当前日期失败: {}", currentDateResult.getMessage());
            }
            
            // 6. 获取当前年份
            Result<Integer> yearResult = timeServiceClient.getCurrentYear(hsfApiKey);
            if (yearResult.isSuccess()) {
                log.info("当前年份: {}", yearResult.getData());
            } else {
                log.error("获取当前年份失败: {}", yearResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("调用时间服务失败: {}", e.getMessage());
        }
    }

}
