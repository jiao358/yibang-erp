package com.yibang.erp.domain.dto;

import lombok.Data;

/**
 * 商品匹配结果DTO
 */
@Data
public class ProductMatchResult {

    /**
     * 是否匹配成功
     */
    private boolean matched;

    /**
     * 匹配的商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 商品规格
     */
    private String specification;

    /**
     * 匹配置信度 (0.0-1.0)
     */
    private double confidence;

    /**
     * 匹配原因
     */
    private String matchReason;

    /**
     * 匹配类型：SKU_EXACT, SKU_FUZZY, NAME_EXACT, NAME_FUZZY, AI_SUGGESTION
     */
    private String matchType;

    /**
     * 供应商公司ID
     */
    private Long supplierCompanyId;

    /**
     * 供应商公司名称
     */
    private String supplierCompanyName;

    /**
     * 商品状态
     */
    private String productStatus;

    /**
     * 建议操作
     */
    private String suggestion;

    /**
     * 扩展信息
     */
    private String extendedInfo;
}
