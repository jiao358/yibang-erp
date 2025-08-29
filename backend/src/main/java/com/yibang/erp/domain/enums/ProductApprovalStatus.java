package com.yibang.erp.domain.enums;

/**
 * 商品审核状态枚举
 * 定义商品的审核状态
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public enum ProductApprovalStatus {
    
    /**
     * 待审核 - 商品等待审核
     */
    PENDING("PENDING", "待审核"),
    
    /**
     * 已通过 - 商品审核通过
     */
    APPROVED("APPROVED", "已通过"),
    
    /**
     * 已拒绝 - 商品审核被拒绝
     */
    REJECTED("REJECTED", "已拒绝");
    
    private final String code;
    private final String description;
    
    ProductApprovalStatus(String code, String description) {
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
    public static ProductApprovalStatus fromCode(String code) {
        for (ProductApprovalStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown product approval status code: " + code);
    }
    
    /**
     * 检查是否为终态状态
     */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
    
    /**
     * 检查是否为审核通过状态
     */
    public boolean isApproved() {
        return this == APPROVED;
    }
    
    /**
     * 检查是否为审核拒绝状态
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
}
