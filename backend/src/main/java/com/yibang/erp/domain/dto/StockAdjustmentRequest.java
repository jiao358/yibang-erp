package com.yibang.erp.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 库存调整请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@Accessors(chain = true)
public class StockAdjustmentRequest {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 调整后可用库存
     */
    private Integer newAvailableQuantity;

    /**
     * 调整后预留库存
     */
    private Integer newReservedQuantity;

    /**
     * 调整后最低库存
     */
    private Integer newMinStockLevel;

    /**
     * 调整原因
     */
    private String reason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
