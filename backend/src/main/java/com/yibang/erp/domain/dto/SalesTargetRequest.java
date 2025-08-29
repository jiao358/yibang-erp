package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售目标请求DTO
 */
@Data
public class SalesTargetRequest {

    private Long id;

    @NotBlank(message = "目标名称不能为空")
    @Size(max = 100, message = "目标名称长度不能超过100个字符")
    private String targetName;

    @NotBlank(message = "目标代码不能为空")
    @Size(max = 50, message = "目标代码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z_][A-Z0-9_]*$", message = "目标代码只能包含大写字母、数字和下划线，且必须以字母或下划线开头")
    private String targetCode;

    @Size(max = 500, message = "目标描述长度不能超过500个字符")
    private String description;

    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    @NotNull(message = "目标值不能为空")
    @DecimalMin(value = "0.0", message = "目标值不能小于0")
    private BigDecimal targetValue;

    @Size(max = 20, message = "目标单位长度不能超过20个字符")
    private String unit;

    @NotBlank(message = "目标周期不能为空")
    private String period;

    @NotNull(message = "周期开始时间不能为空")
    private LocalDateTime periodStart;

    @NotNull(message = "周期结束时间不能为空")
    private LocalDateTime periodEnd;

    @NotBlank(message = "目标状态不能为空")
    private String status;

    @Min(value = 1, message = "优先级必须大于0")
    @Max(value = 999, message = "优先级不能超过999")
    private Integer priority;

    @NotNull(message = "是否启用不能为空")
    private Boolean isActive;

    private Long categoryId;

    @Size(max = 50, message = "客户类型长度不能超过50个字符")
    private String customerType;

    @NotNull(message = "负责人ID不能为空")
    private Long ownerId;

    @NotNull(message = "公司ID不能为空")
    private Long companyId;

    @NotNull(message = "创建人ID不能为空")
    private Long createdBy;
}
