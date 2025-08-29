package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("roles")
public class Role {

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    @TableField("name")
    private String name;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * 权限配置（JSON格式）
     */
    @TableField("permissions")
    private String permissionsConfig;

    /**
     * 是否系统角色
     */
    @TableField("is_system")
    private Boolean isSystem;

    /**
     * 状态：ACTIVE-激活, INACTIVE-未激活
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
    
    /**
     * 角色代码
     */
    @TableField(exist = false)
    private String roleCode;
    
    /**
     * 权限列表
     */
    @TableField(exist = false)
    private Set<Permission> permissions;
}
