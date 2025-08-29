import request from '@/utils/request'
import type { Role, RoleForm } from '@/types/role'

// 角色管理API接口
export const roleApi = {
  // 获取角色列表
  getRoleList(params?: any) {
    return request.get('/api/roles', { params })
  },

  // 根据ID获取角色
  getRoleById(id: number) {
    return request.get(`/api/roles/${id}`)
  },

  // 创建角色
  createRole(data: RoleForm) {
    return request.post('/api/roles', data)
  },

  // 更新角色
  updateRole(id: number, data: RoleForm) {
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
