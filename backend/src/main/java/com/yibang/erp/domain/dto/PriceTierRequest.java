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

    @NotBlank(message = "分层类型不能为空")
    private String tierType;

    @Min(value = 1, message = "分层级别必须大于0")
    @Max(value = 10, message = "分层级别不能超过10")
    private Integer tierLevel;

    @Size(max = 500, message = "分层描述长度不能超过500个字符")
    private String description;

    @DecimalMin(value = "0.0", message = "折扣率不能小于0")
    @DecimalMax(value = "2.0", message = "折扣率不能大于2")
    private BigDecimal discountRate;

    @DecimalMin(value = "0.0", message = "加价率不能小于0")
    @DecimalMax(value = "2.0", message = "加价率不能大于2")
    private BigDecimal markupRate;

    @DecimalMin(value = "0.0", message = "最小订单金额不能小于0")
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0.0", message = "最大订单金额不能小于0")
    private BigDecimal maxOrderAmount;

    @Min(value = 0, message = "最小订单数量不能小于0")
    private Integer minOrderQuantity;

    @Min(value = 0, message = "最大订单数量不能小于0")
    private Integer maxOrderQuantity;

    @Size(max = 20, message = "客户等级要求长度不能超过20个字符")
    private String customerLevelRequirement;

    @Size(max = 100, message = "付款条件长度不能超过100个字符")
    private String paymentTerms;

    @DecimalMin(value = "0.0", message = "信用额度不能小于0")
    private BigDecimal creditLimit;

    private LocalDateTime effectiveStart;

    private LocalDateTime effectiveEnd;

    @NotNull(message = "是否激活不能为空")
    private Boolean isActive;

    @Min(value = 0, message = "优先级不能小于0")
    @Max(value = 999, message = "优先级不能超过999")
    private Integer priority;

    @NotNull(message = "公司ID不能为空")
    private Long companyId;

    @NotNull(message = "创建人ID不能为空")
    private Long createdBy;
}
