package com.yibang.erp.domain.enums;

import lombok.Getter;

/**
 * 价格策略类型枚举
 */
@Getter
public enum PriceStrategyType {

    DISCOUNT("DISCOUNT", "折扣策略"),
    MARKUP("MARKUP", "加价策略"),
    FIXED("FIXED", "固定价格"),
    DYNAMIC("DYNAMIC", "动态定价");

    private final String code;
    private final String description;

    PriceStrategyType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PriceStrategyType fromCode(String code) {
        for (PriceStrategyType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown price strategy type: " + code);
    }

    public static boolean isValid(String code) {
        for (PriceStrategyType type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
