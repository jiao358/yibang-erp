package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.AIExcelProcessRequest;
import com.yibang.erp.domain.dto.AIExcelProcessResponse;
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
}
