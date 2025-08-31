package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 仓库请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class WarehouseRequest {

    /**
     * 仓库编码
     */
    @NotBlank(message = "仓库编码不能为空")
    @Size(max = 50, message = "仓库编码长度不能超过50个字符")
    private String warehouseCode;

    /**
     * 仓库名称
     */
    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 100, message = "仓库名称长度不能超过100个字符")
    private String warehouseName;

    /**
     * 仓库类型：MAIN-主仓库，BRANCH-分仓库，TEMPORARY-临时仓库
     */
    @NotBlank(message = "仓库类型不能为空")
    @Pattern(regexp = "^(MAIN|BRANCH|TEMPORARY)$", message = "仓库类型只能是MAIN、BRANCH或TEMPORARY")
    private String warehouseType;

    /**
     * 所属公司ID
     */
    @NotNull(message = "所属公司ID不能为空")
    private Long companyId;

    /**
     * 仓库地址
     */
    @Size(max = 200, message = "仓库地址长度不能超过200个字符")
    private String address;

    /**
     * 联系人
     */
    @Size(max = 50, message = "联系人长度不能超过50个字符")
    private String contactPerson;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    /**
     * 仓库面积（平方米）
     */
    private Double area;

    /**
     * 仓库状态：ACTIVE-启用，INACTIVE-停用，MAINTENANCE-维护中
     */
    @Pattern(regexp = "^(ACTIVE|INACTIVE|MAINTENANCE)$", message = "仓库状态只能是ACTIVE、INACTIVE或MAINTENANCE")
    private String status = "ACTIVE";

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String description;
}
