package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 库存盘点请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class InventoryCheckRequest {

    /**
     * 盘点类型：FULL-全面盘点，PARTIAL-部分盘点，CYCLE-周期盘点，RANDOM-随机盘点
     */
    @NotNull(message = "盘点类型不能为空")
    @Pattern(regexp = "^(FULL|PARTIAL|CYCLE|RANDOM)$", message = "盘点类型不正确")
    private String checkType;

    /**
     * 仓库ID
     */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /**
     * 计划开始时间
     */
    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     */
    @NotNull(message = "计划结束时间不能为空")
    private LocalDateTime plannedEndTime;

    /**
     * 盘点人ID
     */
    @NotNull(message = "盘点人ID不能为空")
    private Long checkerId;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 盘点说明
     */
    @Size(max = 500, message = "盘点说明长度不能超过500个字符")
    private String description;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
