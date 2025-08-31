package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 订单批量处理请求DTO
 */
@Data
public class OrderBatchProcessRequest {

    /**
     * 处理类型
     */
    @NotBlank(message = "处理类型不能为空")
    private String processType; // SUBMIT, CANCEL, UPDATE_STATUS, VALIDATE, ASSIGN_SUPPLIER

    /**
     * 订单ID列表
     */
    @NotEmpty(message = "订单ID列表不能为空")
    private List<Long> orderIds;

    /**
     * 新状态（用于状态更新）
     */
    private String newStatus;

    /**
     * 变更原因（用于状态更新）
     */
    private String reason;

    /**
     * 供应商公司ID（用于分配供应商）
     */
    private Long supplierCompanyId;

    /**
     * 批量处理参数（JSON格式）
     */
    private String processParams;

    /**
     * 处理备注
     */
    private String remarks;

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

    /**
     * 是否异步处理
     */
    private Boolean asyncProcess = false;

    /**
     * 处理类型枚举
     */
    public enum ProcessType {
        /**
         * 批量提交订单
         */
        SUBMIT("SUBMIT", "批量提交", "批量将草稿订单提交"),

        /**
         * 批量取消订单
         */
        CANCEL("CANCEL", "批量取消", "批量取消订单"),

        /**
         * 批量更新状态
         */
        UPDATE_STATUS("UPDATE_STATUS", "批量更新状态", "批量更新订单状态"),

        /**
         * 批量验证订单
         */
        VALIDATE("VALIDATE", "批量验证", "批量验证订单数据完整性"),

        /**
         * 批量分配供应商
         */
        ASSIGN_SUPPLIER("ASSIGN_SUPPLIER", "批量分配供应商", "批量将订单分配给指定供应商");

        private final String code;
        private final String name;
        private final String description;

        ProcessType(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public static ProcessType fromCode(String code) {
            for (ProcessType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown process type code: " + code);
        }
    }
}
