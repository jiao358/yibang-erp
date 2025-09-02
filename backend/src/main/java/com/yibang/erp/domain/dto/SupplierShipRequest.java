package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 供应商发货请求DTO
 */
@Data
public class SupplierShipRequest {

    /**
     * 物流单号
     */
    @NotBlank(message = "物流单号不能为空")
    private String trackingNumber;

    /**
     * 物流公司
     */
    @NotBlank(message = "物流公司不能为空")
    private String carrier;

    /**
     * 物流公司代码
     */
    private String carrierCode;

    /**
     * 运输方式
     */
    private String shippingMethod;

    /**
     * 发货备注
     */
    private String shippingNotes;

    /**
     * 操作人ID
     */
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作人角色
     */
    private String operatorRole;
}
