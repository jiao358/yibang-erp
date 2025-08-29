package com.yibang.erp.domain.service;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.PriceStrategyRequest;
import com.yibang.erp.domain.dto.PriceStrategyResponse;
import com.yibang.erp.domain.dto.PriceStrategyQueryRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格策略Service接口
 */
public interface PriceStrategyService {

    /**
     * 创建价格策略
     */
    PriceStrategyResponse createPriceStrategy(PriceStrategyRequest request);

    /**
     * 更新价格策略
     */
    PriceStrategyResponse updatePriceStrategy(Long id, PriceStrategyRequest request);

    /**
     * 删除价格策略
     */
    boolean deletePriceStrategy(Long id);

    /**
     * 根据ID获取价格策略
     */
    PriceStrategyResponse getPriceStrategyById(Long id);

    /**
     * 分页查询价格策略
     */
    PageResult<PriceStrategyResponse> getPriceStrategyPage(PriceStrategyQueryRequest request);

    /**
     * 根据条件查询价格策略列表
     */
    List<PriceStrategyResponse> getPriceStrategyList(PriceStrategyQueryRequest request);

    /**
     * 启用/禁用价格策略
     */
    boolean togglePriceStrategyStatus(Long id, Boolean isActive);

    /**
     * 批量启用/禁用价格策略
     */
    boolean batchTogglePriceStrategyStatus(List<Long> ids, Boolean isActive);

    /**
     * 根据商品分类和客户类型获取适用的价格策略
     */
    List<PriceStrategyResponse> getApplicablePriceStrategies(Long categoryId, String customerType, Long companyId);

    /**
     * 应用价格策略计算最终价格
     */
    BigDecimal applyPriceStrategy(Long strategyId, BigDecimal originalPrice);

    /**
     * 批量应用价格策略
     */
    List<BigDecimal> batchApplyPriceStrategies(List<Long> strategyIds, List<BigDecimal> originalPrices);

    /**
     * 验证价格策略配置的有效性
     */
    boolean validatePriceStrategyConfig(PriceStrategyRequest request);

    /**
     * 获取策略类型统计信息
     */
    Object getStrategyTypeStatistics(Long companyId);
}
