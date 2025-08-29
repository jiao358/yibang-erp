package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 权限仓库接口
 */
@Mapper
public interface PermissionRepository extends BaseMapper<Permission> {
    
    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色ID列表查询权限列表
     *
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<Permission> findByRoleIds(@Param("roleIds") List<Long> roleIds);
    
    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据权限代码查询权限
     *
     * @param permissionCode 权限代码
     * @return 权限对象
     */
    Permission findByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 根据模块名称查询权限列表
     *
     * @param moduleName 模块名称
     * @return 权限列表
     */
    List<Permission> findByModuleName(@Param("moduleName") String moduleName);
    
    /**
     * 根据公司ID查询权限列表
     *
     * @param companyId 公司ID
     * @return 权限列表
     */
    List<Permission> findByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * 查询树形权限结构
     *
     * @param companyId 公司ID
     * @return 权限树
     */
    List<Permission> findPermissionTree(@Param("companyId") Long companyId);
    
    /**
     * 根据权限代码列表查询权限
     *
     * @param permissionCodes 权限代码列表
     * @return 权限列表
     */
    List<Permission> findByPermissionCodes(@Param("permissionCodes") Set<String> permissionCodes);
}
