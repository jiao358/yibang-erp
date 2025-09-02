package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.AIExcelProcessRequest;
import com.yibang.erp.domain.dto.AIExcelProcessResponse;
import com.yibang.erp.domain.dto.TaskStatisticsResponse;
import com.yibang.erp.domain.dto.TaskListResponse;
import com.yibang.erp.domain.dto.FailedOrdersResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI驱动的Excel订单处理服务接口
 * 完全独立于现有的批量处理服务
 */
public interface AIExcelOrderService {

    /**
     * 启动AI Excel订单处理任务
     * @param file Excel文件
     * @param request 处理请求参数
     * @return 任务ID和处理状态
     */
    AIExcelProcessResponse startAIExcelProcess(MultipartFile file, AIExcelProcessRequest request);

    /**
     * 获取AI处理任务进度
     * @param taskId 任务ID
     * @return 任务进度信息
     */
    AIExcelProcessResponse getTaskProgress(String taskId);

    /**
     * 取消AI处理任务
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    boolean cancelTask(String taskId);

    /**
     * 获取任务处理结果
     * @param taskId 任务ID
     * @return 处理结果详情
     */
    AIExcelProcessResponse getTaskResult(String taskId);

    /**
     * 获取用户任务列表
     * @param userId 用户ID
     * @param status 任务状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 任务列表响应
     */
    TaskListResponse getUserTasks(Long userId, String status, Integer page, Integer size);

    /**
     * 获取任务统计信息
     * @param companyId 公司ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 任务统计信息
     */
    TaskStatisticsResponse getTaskStatistics(Long companyId, String startDate, String endDate);

    /**
     * 获取失败订单列表
     * @param taskId 任务ID
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段（可选）
     * @param sortOrder 排序方向（可选）
     * @return 失败订单列表响应
     */
    FailedOrdersResponse getFailedOrders(String taskId, Integer page, Integer size, String sortBy, String sortOrder);
}
