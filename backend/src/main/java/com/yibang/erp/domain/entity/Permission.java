package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("permissions")
public class Permission {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 权限名称
     */
    @TableField("permission_name")
    private String permissionName;
    
    /**
     * 权限标识符
     */
    @TableField("permission_code")
    private String permissionCode;
    
    /**
     * 权限类型
     */
    @TableField("permission_type")
    private String permissionType;
    
    /**
     * 权限描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 模块名称
     */
    @TableField("module_name")
    private String moduleName;
    
    /**
     * 操作名称
     */
    @TableField("action_name")
    private String actionName;
    
    /**
     * 权限URL
     */
    @TableField("permission_url")
    private String permissionUrl;
    
    /**
     * HTTP方法
     */
    @TableField("http_method")
    private String httpMethod;
    
    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /**
     * 父权限ID
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 公司ID
     */
    @TableField("company_id")
    private Long companyId;
    
    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除标识
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
    
    /**
     * 子权限列表
     */
    @TableField(exist = false)
    private Set<Permission> children;
    
    /**
     * 角色列表
     */
    @TableField(exist = false)
    private Set<Role> roles;
}
