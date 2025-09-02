package com.yibang.erp.domain.dto;

import lombok.Data;

/**
 * 客户匹配请求DTO
 */
@Data
public class CustomerMatchRequest {

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 匹配类型：CODE_ONLY, NAME_ONLY, PHONE_ONLY, SMART_MATCH
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
     * 是否包含已停用客户
     */
    private Boolean includeInactive = false;
}
