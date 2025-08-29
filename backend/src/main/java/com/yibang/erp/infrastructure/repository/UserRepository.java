package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User findByEmail(@Param("email") String email);
    
    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户对象
     */
    User findByPhone(@Param("phone") String phone);
    
    /**
     * 根据公司ID查询用户列表
     *
     * @param companyId 公司ID
     * @return 用户列表
     */
    java.util.List<User> findByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * 根据角色ID查询用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    java.util.List<User> findByRoleId(@Param("roleId") Long roleId);
}
