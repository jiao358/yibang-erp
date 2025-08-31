package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 库存操作请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class StockOperationRequest {

    /**
     * 操作类型：STOCK_IN-入库，STOCK_OUT-出库，ADJUSTMENT-调整，TRANSFER-调拨
     */
    @NotNull(message = "操作类型不能为空")
    @Pattern(regexp = "^(STOCK_IN|STOCK_OUT|ADJUSTMENT|TRANSFER)$", message = "操作类型只能是STOCK_IN、STOCK_OUT、ADJUSTMENT或TRANSFER")
    private String operationType;

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
     * 操作数量
     */
    @NotNull(message = "操作数量不能为空")
    @Positive(message = "操作数量必须大于0")
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 关联订单ID（如果是订单相关操作）
     */
    private Long orderId;

    /**
     * 关联订单项ID
     */
    private Long orderItemId;

    /**
     * 操作原因
     */
    @Size(max = 200, message = "操作原因长度不能超过200个字符")
    private String reason;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 操作人ID
     */
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;
}
