package com.yibang.erp.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Role;

import java.util.List;
import java.util.Map;

/**
 * 角色服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface RoleService {

    /**
     * 分页查询角色列表
     *
     * @param page 分页参数
     * @param name 角色名称（可选）
     * @param status 状态（可选）
     * @return 分页角色列表
     */
    IPage<Role> getRolePage(Page<Role> page, String name, String status);

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    Role getRoleById(Long id);

    /**
     * 根据名称获取角色
     *
     * @param name 角色名称
     * @return 角色信息
     */
    Role getRoleByName(String name);

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 创建后的角色
     */
    Role createRole(Role role);

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 更新后的角色
     */
    Role updateRole(Role role);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 是否删除成功
     */
    boolean deleteRole(Long id);

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     * @return 删除成功的数量
     */
    int batchDeleteRoles(List<Long> ids);

    /**
     * 更新角色状态
     *
     * @param id 角色ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateRoleStatus(Long id, String status);

    /**
     * 获取所有角色列表
     *
     * @return 角色列表
     */
    List<Role> getAllRoles();

    /**
     * 获取激活状态的角色列表
     *
     * @return 激活的角色列表
     */
    List<Role> getActiveRoles();

    /**
     * 获取所有激活状态的角色（返回Map格式，用于前端显示）
     *
     * @return 角色信息Map列表
     */
    List<Map<String, Object>> getAllActiveRoles();

    /**
     * 根据角色名称获取角色列表
     *
     * @param name 角色名称
     * @return 角色信息Map列表
     */
    List<Map<String, Object>> getRolesByName(String name);
}
