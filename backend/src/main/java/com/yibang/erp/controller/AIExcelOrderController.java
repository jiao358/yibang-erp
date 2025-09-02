package com.yibang.erp.controller;

import com.yibang.erp.common.util.UserSecurityUtils;
import com.yibang.erp.domain.dto.AIExcelProcessRequest;
import com.yibang.erp.domain.dto.AIExcelProcessResponse;
import com.yibang.erp.domain.service.AIExcelOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI Excel订单处理控制器
 * 完全独立于现有的批量处理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai-excel-orders")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES_ADMIN', 'SALES')")
public class AIExcelOrderController {

    @Autowired
    private AIExcelOrderService aiExcelOrderService;

    /**
     * 启动AI Excel订单处理任务
     */
    @PostMapping("/process")
    public ResponseEntity<AIExcelProcessResponse> startAIExcelProcess(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "salesUserId", required = false) Long salesUserId,
            @RequestParam(value = "salesCompanyId", required = false) Long salesCompanyId,
            @RequestParam(value = "priority", required = false, defaultValue = "3") Integer priority,
            @RequestParam(value = "enableAIProductMatching", required = false, defaultValue = "true") Boolean enableAIProductMatching,
            @RequestParam(value = "minConfidenceThreshold", required = false, defaultValue = "0.7") Double minConfidenceThreshold,
            @RequestParam(value = "autoCreateCustomer", required = false, defaultValue = "false") Boolean autoCreateCustomer,
            @RequestParam(value = "autoCreateProduct", required = false, defaultValue = "false") Boolean autoCreateProduct,
            @RequestParam(value = "remarks", required = false) String remarks) {

        try {
            log.info("开始AI Excel订单处理，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

            // 构建请求对象
            AIExcelProcessRequest request = new AIExcelProcessRequest();
            request.setSalesUserId(salesUserId);
            request.setSalesCompanyId(salesCompanyId);
            request.setPriority(priority);
            request.setEnableAIProductMatching(enableAIProductMatching);
            request.setMinConfidenceThreshold(minConfidenceThreshold);
            request.setAutoCreateCustomer(autoCreateCustomer);
            request.setAutoCreateProduct(autoCreateProduct);
            request.setRemarks(remarks);
            request.setOperatorName(UserSecurityUtils.getCurrentUsername());

            // 启动处理任务
            AIExcelProcessResponse response = aiExcelOrderService.startAIExcelProcess(file, request);

            log.info("AI Excel订单处理任务已启动，任务ID: {}", response.getTaskId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("启动AI Excel订单处理任务失败", e);
            AIExcelProcessResponse errorResponse = new AIExcelProcessResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("启动处理任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取AI处理任务进度
     */
    @GetMapping("/progress/{taskId}")
    public ResponseEntity<AIExcelProcessResponse> getTaskProgress(@PathVariable String taskId) {
        try {
            log.info("获取任务进度，任务ID: {}", taskId);
            AIExcelProcessResponse response = aiExcelOrderService.getTaskProgress(taskId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取任务进度失败，任务ID: {}", taskId, e);
            AIExcelProcessResponse errorResponse = new AIExcelProcessResponse();
            errorResponse.setTaskId(taskId);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("获取任务进度失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 取消AI处理任务
     */
    @PostMapping("/cancel/{taskId}")
    public ResponseEntity<AIExcelProcessResponse> cancelTask(@PathVariable String taskId) {
        try {
            log.info("取消任务，任务ID: {}", taskId);
            boolean cancelled = aiExcelOrderService.cancelTask(taskId);

            AIExcelProcessResponse response = new AIExcelProcessResponse();
            response.setTaskId(taskId);

            if (cancelled) {
                response.setStatus("CANCELLED");
                response.setMessage("任务已成功取消");
                log.info("任务已成功取消，任务ID: {}", taskId);
            } else {
                response.setStatus("FAILED");
                response.setMessage("任务取消失败，可能任务已完成或不存在");
                log.warn("任务取消失败，任务ID: {}", taskId);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("取消任务失败，任务ID: {}", taskId, e);
            AIExcelProcessResponse errorResponse = new AIExcelProcessResponse();
            errorResponse.setTaskId(taskId);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("取消任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取AI处理任务结果
     */
    @GetMapping("/result/{taskId}")
    public ResponseEntity<AIExcelProcessResponse> getTaskResult(@PathVariable String taskId) {
        try {
            log.info("获取任务结果，任务ID: {}", taskId);
            AIExcelProcessResponse response = aiExcelOrderService.getTaskResult(taskId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取任务结果失败，任务ID: {}", taskId, e);
            AIExcelProcessResponse errorResponse = new AIExcelProcessResponse();
            errorResponse.setTaskId(taskId);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("获取任务结果失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取当前用户的AI处理任务列表
     */
    @GetMapping("/tasks")
    public ResponseEntity<AIExcelProcessResponse> getUserTasks(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        try {
            log.info("获取用户任务列表，状态: {}, 页码: {}, 大小: {}", status, page, size);
            
            // TODO: 实现获取用户任务列表的逻辑
            AIExcelProcessResponse response = new AIExcelProcessResponse();
            response.setMessage("获取任务列表功能待实现");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取用户任务列表失败", e);
            AIExcelProcessResponse errorResponse = new AIExcelProcessResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("获取任务列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
