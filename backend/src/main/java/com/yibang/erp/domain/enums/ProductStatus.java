package com.yibang.erp.domain.enums;

/**
 * 商品状态枚举
 * 定义商品的生命周期状态
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public enum ProductStatus {
    
    /**
     * 草稿 - 商品信息未完善，处于编辑状态
     */
    DRAFT("DRAFT", "草稿"),
    
    /**
     * 待审核 - 商品信息已完善，等待审核
     */
    PENDING("PENDING", "待审核"),
    
    /**
     * 已上架 - 审核通过，商品已上架销售
     */
    ACTIVE("ACTIVE", "已上架"),
    
    /**
     * 已下架 - 商品暂时下架，不再销售
     */
    INACTIVE("INACTIVE", "已下架"),
    
    /**
     * 已停售 - 商品永久停售，不再上架
     */
    DISCONTINUED("DISCONTINUED", "已停售");
    
    private final String code;
    private final String description;
    
    ProductStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     */
    public static ProductStatus fromCode(String code) {
        for (ProductStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown product status code: " + code);
    }
    
    /**
     * 检查是否为可编辑状态
     */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING;
    }
    
    /**
     * 检查是否为可销售状态
     */
    public boolean isSellable() {
        return this == ACTIVE;
    }
    
    /**
     * 检查是否为终态状态
     */
    public boolean isFinal() {
        return this == DISCONTINUED;
    }
}
