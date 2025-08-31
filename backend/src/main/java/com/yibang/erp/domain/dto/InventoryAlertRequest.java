package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 库存预警请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class InventoryAlertRequest {

    /**
     * 预警类型：LOW_STOCK-库存不足，OVER_STOCK-库存过高，EXPIRY-即将过期，DAMAGED-损坏库存
     */
    @NotNull(message = "预警类型不能为空")
    @Pattern(regexp = "^(LOW_STOCK|OVER_STOCK|EXPIRY|DAMAGED)$", message = "预警类型不正确")
    private String alertType;

    /**
     * 预警级别：LOW-低，MEDIUM-中，HIGH-高，CRITICAL-紧急
     */
    @NotNull(message = "预警级别不能为空")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "预警级别不正确")
    private String alertLevel;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 仓库ID
     */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /**
     * 当前库存数量
     */
    @NotNull(message = "当前库存数量不能为空")
    private Integer currentQuantity;

    /**
     * 预警阈值
     */
    @NotNull(message = "预警阈值不能为空")
    private Integer thresholdQuantity;

    /**
     * 预警内容
     */
    @Size(max = 500, message = "预警内容长度不能超过500个字符")
    private String alertContent;

    /**
     * 预警状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，IGNORED-已忽略
     */
    @Pattern(regexp = "^(PENDING|PROCESSING|RESOLVED|IGNORED)$", message = "预警状态不正确")
    private String status = "PENDING";

    /**
     * 处理结果
     */
    @Size(max = 200, message = "处理结果长度不能超过200个字符")
    private String handlingResult;

    /**
     * 处理备注
     */
    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    private String handlingRemark;
}
