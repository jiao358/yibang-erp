import request from '@/utils/request'
import type { User, Role, Company } from '@/types/user'

// 用户管理API接口
export const userApi = {
  // 分页获取用户列表
  getUserList(params: {
    page: number
    size: number
    username?: string
    realName?: string
    status?: string
  }) {
    return request({
      url: '/api/users',
      method: 'get',
      params
    })
  },

  // 根据ID获取用户
  getUserById(id: number) {
    return request({
      url: `/api/users/${id}`,
      method: 'get'
    })
  },

  // 创建用户
  createUser(data: Partial<User>) {
    return request({
      url: '/api/users',
      method: 'post',
      data
    })
  },

  // 更新用户
  updateUser(id: number, data: Partial<User>) {
    return request({
      url: `/api/users/${id}`,
      method: 'put',
      data
    })
  },

  // 删除用户
  deleteUser(id: number) {
    return request({
      url: `/api/users/${id}`,
      method: 'delete'
    })
  },

  // 批量删除用户
  batchDeleteUsers(ids: number[]) {
    return request({
      url: '/api/users/batch',
      method: 'delete',
      data: ids
    })
  },

  // 更新用户状态
  updateUserStatus(id: number, status: string) {
    return request({
      url: `/api/users/${id}/status`,
      method: 'patch',
      params: { status }
    })
  },

  // 重置用户密码
  resetPassword(id: number, newPassword: string) {
    return request({
      url: `/api/users/${id}/password`,
      method: 'patch',
      params: { newPassword }
    })
  },

  // 获取所有用户
  getAllUsers() {
    return request({
      url: '/api/users/all',
      method: 'get'
    })
  }
}

// 角色管理API接口
export const roleApi = {
  // 获取角色列表
  getRoleList() {
    return request({
      url: '/api/roles',
      method: 'get'
    })
  },

  // 根据ID获取角色
  getRoleById(id: number) {
    return request({
      url: `/api/roles/${id}`,
      method: 'get'
    })
  },

  // 创建角色
  createRole(data: Partial<Role>) {
    return request({
      url: '/api/roles',
      method: 'post',
      data
    })
  },

  // 更新角色
  updateRole(id: number, data: Partial<Role>) {
    return request({
      url: `/api/roles/${id}`,
      method: 'put',
      data
    })
  },

  // 删除角色
  deleteRole(id: number) {
    return request({
      url: `/api/roles/${id}`,
      method: 'delete'
    })
  }
}

// 公司管理API接口
export const companyApi = {
  // 获取公司列表
  getCompanyList() {
    return request({
      url: '/api/companies',
      method: 'get'
    })
  },

  // 根据ID获取公司
  getCompanyById(id: number) {
    return request({
      url: `/api/companies/${id}`,
      method: 'get'
    })
  },

  // 创建公司
  createCompany(data: Partial<Company>) {
    return request({
      url: '/api/companies',
      method: 'post',
      data
    })
  },

  // 更新公司
  updateCompany(id: number, data: Partial<Company>) {
    return request({
      url: `/api/companies/${id}`,
      method: 'put',
      data
    })
  },

  // 删除公司
  deleteCompany(id: number) {
    return request({
      url: `/api/companies/${id}`,
      method: 'delete'
    })
  }
}

// 导出常用方法
export const {
  getUserList,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
  updateUserStatus,
  resetPassword,
  getAllUsers
} = userApi

export const {
  getRoleList,
  getRoleById,
  createRole,
  updateRole,
  deleteRole
} = roleApi

export const {
  getCompanyList,
  getCompanyById,
  createCompany,
  updateCompany,
  deleteCompany
} = companyApi
