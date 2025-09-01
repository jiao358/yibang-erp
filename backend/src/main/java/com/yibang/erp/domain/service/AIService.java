package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.AIOrderProcessRequest;
import com.yibang.erp.domain.dto.AIOrderProcessResult;
import com.yibang.erp.domain.dto.AIConfigRequest;
import com.yibang.erp.domain.dto.AIConfigResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI服务接口
 * 提供AI订单处理、配置管理等功能
 */
public interface AIService {
    
    /**
     * 处理AI订单
     * @param request AI订单处理请求
     * @return AI订单处理结果
     */
    AIOrderProcessResult processAIOrder(AIOrderProcessRequest request);
    
    /**
     * 批量处理AI订单
     * @param requests 批量AI订单处理请求
     * @return 批量处理结果
     */
    List<AIOrderProcessResult> batchProcessAIOrders(List<AIOrderProcessRequest> requests);
    
    /**
     * 从Excel文件批量处理订单
     * @param file Excel文件
     * @param userId 销售ID
     * @return 批量处理结果
     */
    List<AIOrderProcessResult> processOrdersFromExcel(MultipartFile file, Long userId);
    
    /**
     * 获取AI配置信息
     * @return AI配置响应
     */
    AIConfigResponse getAIConfig();
    
    /**
     * 更新AI配置
     * @param request AI配置请求
     * @return 更新后的配置
     */
    AIConfigResponse updateAIConfig(AIConfigRequest request);
    
    /**
     * 测试AI连接
     * @return 连接测试结果
     */
    boolean testAIConnection();
    
    /**
     * 获取AI处理统计信息
     * @param companyId 公司ID
     * @return 统计信息
     */
    Object getAIProcessStatistics(Long companyId);
    
    /**
     * 获取AI处理历史
     * @param companyId 公司ID
     * @param page 页码
     * @param size 页大小
     * @return 处理历史列表
     */
    List<AIOrderProcessResult> getAIProcessHistory(Long companyId, int page, int size);
}
