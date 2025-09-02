package com.yibang.erp.domain.dto;

import lombok.Data;

/**
 * 商品匹配请求DTO
 */
@Data
public class ProductMatchRequest {

    /**
     * 商品SKU/69码
     */
    private String sku;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品规格
     */
    private String specification;

    /**
     * 匹配类型：SKU_ONLY, NAME_ONLY, SMART_MATCH
     */
    private String matchType = "SMART_MATCH";

    /**
     * 最小置信度阈值
     */
    private Double minConfidence = 0.7;

    /**
     * 最大返回结果数
     */
    private Integer maxResults = 5;

    /**
     * 是否包含已下架商品
     */
    private Boolean includeInactive = false;
}
