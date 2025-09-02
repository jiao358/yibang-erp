package com.yibang.erp.controller;

import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.common.util.UserSecurityUtils;
import com.yibang.erp.domain.dto.*;
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
    @Autowired
    private JwtUtil jwtUtil;

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
     * 获取任务统计信息
     * 基于现有数据库表 ai_excel_process_tasks 进行统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<TaskStatisticsResponse> getTaskStatistics(
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {

        try {
            log.info("获取任务统计信息，公司ID: {}, 开始日期: {}, 结束日期: {}", companyId, startDate, endDate);
            
            // 调用服务层获取真实统计数据
            TaskStatisticsResponse response = aiExcelOrderService.getTaskStatistics(companyId, startDate, endDate);
            
            log.info("返回统计数据: 总数={}, 处理中={}, 完成={}, 失败={}", 
                    response.getTotalTasks(), response.getProcessingTasks(), 
                    response.getCompletedTasks(), response.getFailedTasks());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取任务统计信息失败", e);
            TaskStatisticsResponse errorResponse = TaskStatisticsResponse.error("获取统计信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取失败订单列表
     */
        @GetMapping("/{taskId}/failed-orders")
    public ResponseEntity<FailedOrdersResponse> getFailedOrders(
            @PathVariable String taskId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "excelRowNumber") String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "asc") String sortOrder) {
        try {
            log.info("获取失败订单列表，任务ID: {}, 页码: {}, 大小: {}, 排序: {} {}",
                    taskId, page, size, sortBy, sortOrder);

            FailedOrdersResponse response = aiExcelOrderService.getFailedOrders(taskId, page, size, sortBy, sortOrder);

            log.info("返回失败订单列表: 总数={}, 当前页={}, 每页大小={}",
                    response.getTotalElements(), response.getCurrentPage(), response.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取失败订单列表失败，任务ID: {}", taskId, e);
            FailedOrdersResponse errorResponse = FailedOrdersResponse.error("获取失败订单列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/{taskId}/success-orders")
    public ResponseEntity<SuccessOrdersResponse> getSuccessOrders(
            @PathVariable String taskId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "created_at") String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "asc") String sortOrder) {
        try {
            log.info("获取成功订单列表，任务ID: {}, 页码: {}, 大小: {}, 排序: {} {}",
                    taskId, page, size, sortBy, sortOrder);

            SuccessOrdersResponse response = aiExcelOrderService.getSuccessOrders(taskId, page, size, sortBy, sortOrder);

            log.info("返回成功订单列表: 总数={}, 当前页={}, 每页大小={}",
                    response.getTotalElements(), response.getCurrentPage(), response.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取成功订单列表失败，任务ID: {}", taskId, e);
            SuccessOrdersResponse errorResponse = SuccessOrdersResponse.error("获取成功订单列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/{taskId}/processing-logs")
    public ResponseEntity<ProcessingLogsResponse> getProcessingLogs(
            @PathVariable String taskId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "sortBy", required = false, defaultValue = "timestamp") String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder) {
        try {
            log.info("获取处理日志列表，任务ID: {}, 页码: {}, 大小: {}, 级别: {}, 排序: {} {}",
                    taskId, page, size, level, sortBy, sortOrder);

            ProcessingLogsResponse response = aiExcelOrderService.getProcessingLogs(taskId, page, size, level, sortBy, sortOrder);

            log.info("返回处理日志列表: 总数={}, 当前页={}, 每页大小={}",
                    response.getTotalElements(), response.getCurrentPage(), response.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取处理日志列表失败，任务ID: {}", taskId, e);
            ProcessingLogsResponse errorResponse = ProcessingLogsResponse.error("获取处理日志列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private Long getCurrentUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("无效的Authorization头");
        }

        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 获取当前用户的AI处理任务列表
     * 基于现有数据库表 ai_excel_process_tasks 进行查询
     */
    @GetMapping("/tasks")
    public ResponseEntity<TaskListResponse> getUserTasks(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestHeader("Authorization") String authorization) {

        try {
            // 获取当前登录用户ID
            Long currentUserId = getCurrentUserId(authorization);

            if (currentUserId == null) {
                log.warn("无法获取当前用户ID");
                return ResponseEntity.badRequest().body(TaskListResponse.error("无法获取当前用户信息"));
            }
            
            log.info("获取用户任务列表，用户ID: {}, 状态: {}, 页码: {}, 大小: {}", 
                    currentUserId, status, page, size);
            
            // 调用服务层获取真实任务列表
            TaskListResponse response = aiExcelOrderService.getUserTasks(currentUserId, status, page, size);
            
            log.info("返回任务列表: 总数={}, 当前页={}, 每页大小={}, 筛选状态={}", 
                    response.getTotalElements(), response.getCurrentPage(), response.getSize(), status);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取用户任务列表失败", e);
            TaskListResponse errorResponse = TaskListResponse.error("获取任务列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
