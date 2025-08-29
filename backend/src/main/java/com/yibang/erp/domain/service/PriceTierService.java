package com.yibang.erp.domain.service;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.PriceTierRequest;
import com.yibang.erp.domain.dto.PriceTierResponse;
import com.yibang.erp.domain.dto.PriceTierQueryRequest;
import com.yibang.erp.domain.entity.PriceTier;

import java.util.List;

/**
 * 价格分层Service接口
 */
public interface PriceTierService {

    /**
     * 创建价格分层
     */
    PriceTierResponse createPriceTier(PriceTierRequest request);

    /**
     * 更新价格分层
     */
    PriceTierResponse updatePriceTier(Long id, PriceTierRequest request);

    /**
     * 删除价格分层
     */
    boolean deletePriceTier(Long id);

    /**
     * 根据ID获取价格分层
     */
    PriceTierResponse getPriceTierById(Long id);

    /**
     * 分页查询价格分层
     */
    PageResult<PriceTierResponse> getPriceTierPage(PriceTierQueryRequest request);

    /**
     * 根据条件查询价格分层列表
     */
    List<PriceTierResponse> getPriceTierList(PriceTierQueryRequest request);

    /**
     * 启用/禁用价格分层
     */
    boolean togglePriceTierStatus(Long id, Boolean isActive);

    /**
     * 批量启用/禁用价格分层
     */
    boolean batchTogglePriceTierStatus(List<Long> ids, Boolean isActive);

    /**
     * 根据商品分类和客户类型获取适用的价格分层
     */
    List<PriceTierResponse> getApplicablePriceTiers(Long categoryId, String customerType, Long companyId);

    /**
     * 计算商品在指定分层下的最终价格
     */
    java.math.BigDecimal calculateFinalPrice(Long priceTierId, java.math.BigDecimal originalPrice);

    /**
     * 验证价格分层配置的有效性
     */
    boolean validatePriceTierConfig(PriceTierRequest request);
}
