package com.yibang.erp.domain.dto;

import lombok.Data;

/**
 * 客户匹配结果DTO
 */
@Data
public class CustomerMatchResult {

    /**
     * 是否匹配成功
     */
    private boolean matched;

    /**
     * 匹配的客户ID
     */
    private Long customerId;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 客户地址
     */
    private String address;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 匹配置信度 (0.0-1.0)
     */
    private double confidence;

    /**
     * 匹配原因
     */
    private String matchReason;

    /**
     * 匹配类型：CODE_EXACT, CODE_FUZZY, NAME_EXACT, NAME_FUZZY, PHONE_EXACT, PHONE_FUZZY, AI_SUGGESTION
     */
    private String matchType;

    /**
     * 客户状态
     */
    private String customerStatus;

    /**
     * 建议操作
     */
    private String suggestion;

    /**
     * 扩展信息
     */
    private String extendedInfo;
}
