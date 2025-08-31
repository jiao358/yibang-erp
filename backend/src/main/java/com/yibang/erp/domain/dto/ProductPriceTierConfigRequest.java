package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 商品价格分层配置请求DTO
 */
@Data
public class ProductPriceTierConfigRequest {

    private Long id;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "价格分层ID不能为空")
    private Long priceTierId;

    @NotNull(message = "一件代发价格不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "一件代发价格必须大于0")
    private BigDecimal dropshippingPrice;

    @NotNull(message = "零售限价不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "零售限价必须大于0")
    private BigDecimal retailLimitPrice;

    @NotNull(message = "是否启用不能为空")
    private Boolean isActive;
    
    /**
     * 公司ID
     */
    private Long companyId;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
}
