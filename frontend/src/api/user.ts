import request from '@/utils/request'
import type { User, Role, Company } from '@/types/user'

// 用户管理API接口
export const userApi = {
  // 分页获取用户列表
  getUserList(params: {
    current: number
    size: number
    username?: string
    personalCompanyName?: string
    email?: string
    phone?: string
    roleName?: string
    status?: string
    companyId?: number
  }) {
    return request.get('/api/users', { params })
  },

  // 根据ID获取用户
  getUserById(id: number) {
    return request.get(`/api/users/${id}`)
  },

  // 创建用户
  createUser(data: Partial<User>) {
    return request.post('/api/users', data)
  },

  // 更新用户
  updateUser(id: number, data: Partial<User>) {
    return request.put(`/api/users/${id}`, data)
  },

  // 删除用户
  deleteUser(id: number) {
    return request.delete(`/api/users/${id}`)
  },

  // 批量删除用户
  batchDeleteUsers(ids: number[]) {
    return request.delete('/api/users/batch', { data: ids })
  },

  // 更新用户状态
  updateUserStatus(id: number, status: string) {
    return request.patch(`/api/users/${id}/status`, null, { params: { status } })
  },

  // 重置用户密码
  resetPassword(id: number, newPassword: string, confirmPassword: string) {
    return request.post(`/api/users/${id}/reset-password`, {
      newPassword,
      confirmPassword
    })
  },

  // 获取所有用户
  getAllUsers() {
    return request.get('/api/users/all')
  },

  // 获取可用角色列表
  getAvailableRoles() {
    return request.get('/api/users/roles/available')
  },

  // 获取可用公司列表
  getAvailableCompanies() {
    return request.get('/api/users/companies/available')
  }
}

// 角色管理API接口
export const roleApi = {
  // 获取角色列表
  getRoleList() {
    return request.get('/api/roles')
  },

  // 根据ID获取角色
  getRoleById(id: number) {
    return request.get(`/api/roles/${id}`)
  },

  // 创建角色
  createRole(data: Partial<Role>) {
    return request.post('/api/roles', data)
  },

  // 更新角色
  updateRole(id: number, data: Partial<Role>) {
    return request.put(`/api/roles/${id}`, data)
  },

  // 删除角色
  deleteRole(id: number) {
    return request.delete(`/api/roles/${id}`)
  },

  // 批量删除角色
  batchDeleteRoles(ids: number[]) {
    return request.delete('/api/roles/batch', { data: ids })
  },

  // 更新角色状态
  updateRoleStatus(id: number, status: string) {
    return request.patch(`/api/roles/${id}/status`, null, { params: { status } })
  },

  // 获取所有角色
  getAllRoles() {
    return request.get('/api/roles/all')
  },

  // 获取活跃角色
  getActiveRoles() {
    return request.get('/api/roles/active')
  }
}

// 公司管理API接口
export const companyApi = {
  // 获取公司列表
  getCompanyList() {
    return request.get('/api/companies')
  },

  // 根据ID获取公司
  getCompanyById(id: number) {
    return request.get(`/api/companies/${id}`)
  },

  // 创建公司
  createCompany(data: Partial<Company>) {
    return request.post('/api/companies', data)
  },

  // 更新公司
  updateCompany(id: number, data: Partial<Company>) {
    return request.put(`/api/companies/${id}`, data)
  },

  // 删除公司
  deleteCompany(id: number) {
    return request.delete(`/api/companies/${id}`)
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
  getAllUsers,
  getAvailableRoles,
  getAvailableCompanies
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
