package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 角色更新请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class RoleUpdateRequest {

    /**
     * 角色名称
     */
    @Size(min = 2, max = 50, message = "角色名称长度在2到50个字符")
    private String name;

    /**
     * 角色描述
     */
    @Size(max = 200, message = "角色描述不能超过200个字符")
    private String description;

    /**
     * 权限配置（JSON格式字符串）
     */
    private String permissionsConfig;

    /**
     * 状态：ACTIVE-激活, INACTIVE-未激活
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
