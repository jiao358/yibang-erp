package com.yibang.erp.domain.enums;

import lombok.Getter;

/**
 * 销售目标类型枚举
 */
@Getter
public enum SalesTargetType {

    REVENUE("REVENUE", "营收目标"),
    QUANTITY("QUANTITY", "数量目标"),
    CUSTOMER("CUSTOMER", "客户数目标"),
    PRODUCT("PRODUCT", "产品目标");

    private final String code;
    private final String description;

    SalesTargetType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SalesTargetType fromCode(String code) {
        for (SalesTargetType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown sales target type: " + code);
    }

    public static boolean isValid(String code) {
        for (SalesTargetType type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
