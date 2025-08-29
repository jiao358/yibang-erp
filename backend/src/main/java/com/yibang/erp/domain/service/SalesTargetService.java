package com.yibang.erp.domain.service;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.SalesTargetRequest;
import com.yibang.erp.domain.dto.SalesTargetResponse;
import com.yibang.erp.domain.dto.SalesTargetQueryRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售目标Service接口
 */
public interface SalesTargetService {

    /**
     * 创建销售目标
     */
    SalesTargetResponse createSalesTarget(SalesTargetRequest request);

    /**
     * 更新销售目标
     */
    SalesTargetResponse updateSalesTarget(Long id, SalesTargetRequest request);

    /**
     * 删除销售目标
     */
    boolean deleteSalesTarget(Long id);

    /**
     * 根据ID获取销售目标
     */
    SalesTargetResponse getSalesTargetById(Long id);

    /**
     * 分页查询销售目标
     */
    PageResult<SalesTargetResponse> getSalesTargetPage(SalesTargetQueryRequest request);

    /**
     * 根据条件查询销售目标列表
     */
    List<SalesTargetResponse> getSalesTargetList(SalesTargetQueryRequest request);

    /**
     * 启用/禁用销售目标
     */
    boolean toggleSalesTargetStatus(Long id, Boolean isActive);

    /**
     * 批量启用/禁用销售目标
     */
    boolean batchToggleSalesTargetStatus(List<Long> ids, Boolean isActive);

    /**
     * 更新目标进度
     */
    boolean updateTargetProgress(Long id, BigDecimal currentValue);

    /**
     * 批量更新目标进度
     */
    boolean batchUpdateTargetProgress(List<Long> ids, List<BigDecimal> currentValues);

    /**
     * 激活销售目标
     */
    boolean activateSalesTarget(Long id);

    /**
     * 完成销售目标
     */
    boolean completeSalesTarget(Long id);

    /**
     * 取消销售目标
     */
    boolean cancelSalesTarget(Long id);

    /**
     * 重置销售目标
     */
    boolean resetSalesTarget(Long id);

    /**
     * 根据商品分类和客户类型获取适用的销售目标
     */
    List<SalesTargetResponse> getApplicableSalesTargets(Long categoryId, String customerType, Long companyId);

    /**
     * 获取目标完成统计信息
     */
    Object getTargetCompletionStatistics(Long companyId);

    /**
     * 获取目标类型统计信息
     */
    Object getTargetTypeStatistics(Long companyId);

    /**
     * 验证销售目标配置的有效性
     */
    boolean validateSalesTargetConfig(SalesTargetRequest request);
}
