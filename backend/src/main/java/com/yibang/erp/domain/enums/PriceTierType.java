package com.yibang.erp.domain.enums;

import lombok.Getter;

/**
 * 价格分层类型枚举
 */
@Getter
public enum PriceTierType {

    BASIC("BASIC", "基础价格"),
    VIP("VIP", "VIP价格"),
    WHOLESALE("WHOLESALE", "批发价格"),
    CUSTOM("CUSTOM", "自定义价格");

    private final String code;
    private final String description;

    PriceTierType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PriceTierType fromCode(String code) {
        for (PriceTierType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown price tier type: " + code);
    }

    public static boolean isValid(String code) {
        for (PriceTierType type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
