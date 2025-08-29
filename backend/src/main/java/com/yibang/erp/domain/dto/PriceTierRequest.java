package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格分层请求DTO
 */
@Data
public class PriceTierRequest {

    private Long id;

    @NotBlank(message = "分层名称不能为空")
    @Size(max = 100, message = "分层名称长度不能超过100个字符")
    private String tierName;

    @NotBlank(message = "分层代码不能为空")
    @Size(max = 50, message = "分层代码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z_][A-Z0-9_]*$", message = "分层代码只能包含大写字母、数字和下划线，且必须以字母或下划线开头")
    private String tierCode;

    @Size(max = 500, message = "分层描述长度不能超过500个字符")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "最低价格必须大于0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "最高价格必须大于0")
    private BigDecimal maxPrice;

    @DecimalMin(value = "0.0", message = "折扣率不能小于0")
    @DecimalMax(value = "1.0", message = "折扣率不能大于1")
    private BigDecimal discountRate;

    @DecimalMin(value = "0.0", message = "固定折扣金额不能小于0")
    private BigDecimal fixedDiscount;

    @NotBlank(message = "分层类型不能为空")
    private String tierType;

    @Min(value = 1, message = "优先级必须大于0")
    @Max(value = 999, message = "优先级不能超过999")
    private Integer priority;

    @NotNull(message = "是否启用不能为空")
    private Boolean isActive;

    private Long categoryId;

    @Size(max = 50, message = "客户类型长度不能超过50个字符")
    private String customerType;

    private LocalDateTime effectiveStart;

    private LocalDateTime effectiveEnd;

    @NotNull(message = "公司ID不能为空")
    private Long companyId;

    @NotNull(message = "创建人ID不能为空")
    private Long createdBy;
}
