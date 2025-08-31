package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 订单冲突解决请求DTO
 */
@Data
public class OrderConflictResolutionRequest {

    /**
     * 解决方式
     */
    @NotBlank(message = "解决方式不能为空")
    private String resolutionType; // MERGE, REPLACE, KEEP_EXISTING, KEEP_NEW

    /**
     * 解决原因
     */
    private String reason;

    /**
     * 解决备注
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
     * 保留的订单ID（当resolutionType为KEEP_EXISTING时使用）
     */
    private Long keepOrderId;

    /**
     * 合并后的订单数据（当resolutionType为MERGE时使用）
     */
    private OrderCreateRequest mergedOrderData;

    /**
     * 解决方式枚举
     */
    public enum ResolutionType {
        /**
         * 合并订单
         */
        MERGE("MERGE", "合并订单", "将冲突的订单数据合并为一个订单"),

        /**
         * 替换现有订单
         */
        REPLACE("REPLACE", "替换现有订单", "用新订单数据替换现有订单"),

        /**
         * 保留现有订单
         */
        KEEP_EXISTING("KEEP_EXISTING", "保留现有订单", "保留现有订单，丢弃新订单"),

        /**
         * 保留新订单
         */
        KEEP_NEW("KEEP_NEW", "保留新订单", "保留新订单，丢弃现有订单");

        private final String code;
        private final String name;
        private final String description;

        ResolutionType(String code, String name, String description) {
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

        public static ResolutionType fromCode(String code) {
            for (ResolutionType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown resolution type code: " + code);
        }
    }
}
