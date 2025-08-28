import request from '@/utils/request'
import type { Role, CreateRoleRequest, UpdateRoleRequest, RoleQueryParams } from '@/types/role'

// 角色管理API接口
export const roleApi = {
  // 分页获取角色列表
  getRoleList(params: RoleQueryParams) {
    return request({
      url: '/api/roles',
      method: 'get',
      params
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
  createRole(data: CreateRoleRequest) {
    return request({
      url: '/api/roles',
      method: 'post',
      data
    })
  },

  // 更新角色
  updateRole(id: number, data: UpdateRoleRequest) {
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
  },

  // 批量删除角色
  batchDeleteRoles(ids: number[]) {
    return request({
      url: '/api/roles/batch',
      method: 'delete',
      data: ids
    })
  },

  // 更新角色状态
  updateRoleStatus(id: number, status: string) {
    return request({
      url: `/api/roles/${id}/status`,
      method: 'patch',
      params: { status }
    })
  },

  // 获取所有角色
  getAllRoles() {
    return request({
      url: '/api/roles/all',
      method: 'get'
    })
  },

  // 获取激活状态的角色列表
  getActiveRoles() {
    return request({
      url: '/api/roles/active',
      method: 'get'
    })
  }
}

// 导出常用方法
export const {
  getRoleList,
  getRoleById,
  createRole,
  updateRole,
  deleteRole,
  batchDeleteRoles,
  updateRoleStatus,
  getAllRoles,
  getActiveRoles
} = roleApi
