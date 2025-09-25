package com.yibang.erp.domain.dto.microservice;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品信息响应DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class ProductInfoResponse {
    
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 商品编码
     */
    private String sku;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品简介
     */
    private String shortDescription;

    /**
     * 供应链公司id
     */
    private Long companyId;

    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 品牌ID
     */
    private Long brandId;
    
    /**
     * 品牌名称
     */
    private String brandName;
    
    /**
     * 商品图片列表
     */
    private List<String> images;
    
    /**
     * 商品规格
     */
    private String specifications;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 成本价
     */
    private BigDecimal costPrice;
    
    /**
     * 销售价
     */
    private BigDecimal sellingPrice;
    
    /**
     * 市场价
     */
    private BigDecimal marketPrice;
    
    /**
     * 零售端限价
     */
    private BigDecimal retailLimitPrice;
    
    /**
     * 重量(kg)
     */
    private BigDecimal weight;
    
    /**
     * 尺寸
     */
    private String dimensions;
    
    /**
     * 颜色
     */
    private String color;
    
    /**
     * 材质
     */
    private String material;
    
    /**
     * 原产国
     */
    private String originCountry;
    
    /**
     * 商品状态
     */
    private String status;
    
    /**
     * 审核状态
     */
    private String approvalStatus;
    
    /**
     * 是否热销
     */
    private Boolean isHot;
    
    /**
     * 是否新品
     */
    private Boolean isNew;
    
    /**
     * 是否推荐
     */
    private Boolean isFeatured;
    
    /**
     * 库存数量
     */
    private Integer stockQuantity;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
