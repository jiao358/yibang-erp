package com.yibang.erp.domain.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    
    /**
     * 草稿状态 - 订单刚创建，未提交
     */
    DRAFT("DRAFT", "草稿", "订单刚创建，未提交"),
    
    /**
     * 已提交 - 销售已提交订单，等待供应商确认
     */
    SUBMITTED("SUBMITTED", "已提交", "销售已提交订单，等待供应商确认"),
    
    /**
     * 供应商确认 - 供应商已确认订单，准备发货
     */
    SUPPLIER_CONFIRMED("SUPPLIER_CONFIRMED", "供应商确认", "供应商已确认订单，准备发货"),
    
    /**
     * 已发货 - 供应商已发货
     */
    SHIPPED("SHIPPED", "已发货", "供应商已发货"),
    
    /**
     * 运输中 - 物流运输中
     */
    IN_TRANSIT("IN_TRANSIT", "运输中", "物流运输中"),
    
    /**
     * 已送达 - 订单已送达客户
     */
    DELIVERED("DELIVERED", "已送达", "订单已送达客户"),
    
    /**
     * 已完成 - 订单完成
     */
    COMPLETED("COMPLETED", "已完成", "订单完成"),
    
    /**
     * 已取消 - 订单已取消
     */
    CANCELLED("CANCELLED", "已取消", "订单已取消"),
    
    /**
     * 已拒绝 - 供应商拒绝订单
     */
    REJECTED("REJECTED", "已拒绝", "供应商拒绝订单");

    private final String code;
    private final String name;
    private final String description;

    OrderStatus(String code, String name, String description) {
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

    /**
     * 根据状态码获取状态枚举
     */
    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }

    /**
     * 检查状态转换是否有效
     */
    public static boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }

        // 草稿状态可以转换为任何状态
        if (from == DRAFT) {
            return true;
        }

        // 已提交状态可以转换为：供应商确认、已取消、已拒绝
        if (from == SUBMITTED) {
            return to == SUPPLIER_CONFIRMED || to == CANCELLED || to == REJECTED;
        }

        // 供应商确认状态可以转换为：已发货、已取消
        if (from == SUPPLIER_CONFIRMED) {
            return to == SHIPPED || to == CANCELLED;
        }

        // 已发货状态可以转换为：运输中
        if (from == SHIPPED) {
            return to == IN_TRANSIT;
        }

        // 运输中状态可以转换为：已送达
        if (from == IN_TRANSIT) {
            return to == DELIVERED;
        }

        // 已送达状态可以转换为：已完成
        if (from == DELIVERED) {
            return to == COMPLETED;
        }

        // 已完成、已取消、已拒绝状态不能转换为其他状态
        if (from == COMPLETED || from == CANCELLED || from == REJECTED) {
            return false;
        }

        return false;
    }

    /**
     * 获取状态转换提示信息
     */
    public static String getTransitionMessage(OrderStatus from, OrderStatus to) {
        if (isValidTransition(from, to)) {
            return String.format("订单状态从 %s 转换为 %s", from.getName(), to.getName());
        } else {
            return String.format("不允许从 %s 状态转换为 %s 状态", from.getName(), to.getName());
        }
    }
}
