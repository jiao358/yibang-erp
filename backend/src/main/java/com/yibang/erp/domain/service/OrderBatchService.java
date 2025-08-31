package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.OrderBatchProcessRequest;
import com.yibang.erp.domain.dto.OrderBatchProcessResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 订单批量处理服务接口
 */
public interface OrderBatchService {

    /**
     * 批量导入Excel订单
     */
    OrderBatchProcessResponse importExcelOrders(MultipartFile file, Long templateId);

    /**
     * 批量更新订单状态
     */
    OrderBatchProcessResponse batchUpdateOrderStatus(OrderBatchProcessRequest request);

    /**
     * 批量删除订单
     */
    OrderBatchProcessResponse batchDeleteOrders(OrderBatchProcessRequest request);

    /**
     * 批量分配供应商
     */
    OrderBatchProcessResponse batchAssignSupplier(OrderBatchProcessRequest request);

    /**
     * 获取批量处理进度
     */
    OrderBatchProcessResponse getBatchProgress(String batchId);

    /**
     * 取消批量处理
     */
    boolean cancelBatchProcess(String batchId);
}
