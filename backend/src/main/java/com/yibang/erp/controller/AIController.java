package com.yibang.erp.controller;

import com.yibang.erp.domain.dto.*;
import com.yibang.erp.domain.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI控制器
 * 提供AI订单处理和配置管理的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    
    /**
     * 处理单个AI订单
     */
    @PostMapping("/orders/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<AIOrderProcessResult> processAIOrder(@RequestBody AIOrderProcessRequest request) {
        log.info("开始处理AI订单: {}", request.getOrderId());
        AIOrderProcessResult result = aiService.processAIOrder(request);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量处理AI订单
     */
    @PostMapping("/orders/batch-process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<AIOrderProcessResult>> batchProcessAIOrders(@RequestBody List<AIOrderProcessRequest> requests) {
        log.info("开始批量处理AI订单，数量: {}", requests.size());
        List<AIOrderProcessResult> results = aiService.batchProcessAIOrders(requests);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 从Excel文件批量处理订单
     */
    @PostMapping("/orders/excel-process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<AIOrderProcessResult>> processOrdersFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companyId") Long companyId) {
        log.info("开始从Excel文件处理订单，公司ID: {}", companyId);
        List<AIOrderProcessResult> results = aiService.processOrdersFromExcel(file, companyId);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 获取AI配置信息
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AIConfigResponse> getAIConfig() {
        log.info("获取AI配置信息");
        AIConfigResponse config = aiService.getAIConfig();
        return ResponseEntity.ok(config);
    }
    
    /**
     * 更新AI配置
     */
    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AIConfigResponse> updateAIConfig(@RequestBody AIConfigRequest request) {
        log.info("更新AI配置，用户ID: {}", request.getConfigUserId());
        AIConfigResponse config = aiService.updateAIConfig(request);
        return ResponseEntity.ok(config);
    }
    
    /**
     * 测试AI连接
     */
    @PostMapping("/config/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> testAIConnection() {
        log.info("测试AI连接");
        boolean result = aiService.testAIConnection();
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取AI处理统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Object> getAIProcessStatistics(@RequestParam Long companyId) {
        log.info("获取AI处理统计信息，公司ID: {}", companyId);
        Object statistics = aiService.getAIProcessStatistics(companyId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取AI处理历史
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<AIOrderProcessResult>> getAIProcessHistory(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取AI处理历史，公司ID: {}, 页码: {}, 页大小: {}", companyId, page, size);
        List<AIOrderProcessResult> history = aiService.getAIProcessHistory(companyId, page, size);
        return ResponseEntity.ok(history);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI服务运行正常");
    }
}
