package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格策略请求DTO
 */
@Data
public class PriceStrategyRequest {

    private Long id;

    @NotBlank(message = "策略名称不能为空")
    @Size(max = 100, message = "策略名称长度不能超过100个字符")
    private String strategyName;

    @NotBlank(message = "策略代码不能为空")
    @Size(max = 50, message = "策略代码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z_][A-Z0-9_]*$", message = "策略代码只能包含大写字母、数字和下划线，且必须以字母或下划线开头")
    private String strategyCode;

    @Size(max = 500, message = "策略描述长度不能超过500个字符")
    private String description;

    @NotBlank(message = "策略类型不能为空")
    private String strategyType;

    @DecimalMin(value = "0.0", message = "基础价格不能小于0")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0", message = "调整比例不能小于0")
    @DecimalMax(value = "10.0", message = "调整比例不能超过10")
    private BigDecimal adjustmentRate;

    @DecimalMin(value = "0.0", message = "调整金额不能小于0")
    private BigDecimal adjustmentAmount;

    @DecimalMin(value = "0.0", message = "最小价格不能小于0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "最大价格不能小于0")
    private BigDecimal maxPrice;

    @Size(max = 1000, message = "适用条件长度不能超过1000个字符")
    private String conditions;

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
