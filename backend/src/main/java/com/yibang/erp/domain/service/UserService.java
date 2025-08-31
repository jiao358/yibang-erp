package com.yibang.erp.domain.service;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.UserQueryRequest;
import com.yibang.erp.domain.dto.UserListResponse;
import com.yibang.erp.domain.entity.User;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface UserService {

    /**
     * 分页查询用户列表
     *
     * @param queryRequest 查询参数（包含分页信息）
     * @return 分页用户列表
     */
    PageResult<User> getUserPage(UserQueryRequest queryRequest);

    /**
     * 分页查询用户列表（带角色和公司信息联查）
     *
     * @param queryRequest 查询参数（包含分页信息）
     * @return 分页用户列表响应
     */
    PageResult<UserListResponse> getUserPageWithRoleAndCompany(UserQueryRequest queryRequest,Long belongCompanyId);

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 创建后的用户
     */
    User createUser(User user);

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 更新后的用户
     */
    User updateUser(User user);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long id);

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 删除成功的数量
     */
    int batchDeleteUsers(List<Long> ids);

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateUserStatus(Long id, String status);

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    List<User> getAllUsers();
}
