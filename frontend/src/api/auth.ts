import request from '@/utils/request'

// 登录请求参数类型
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应数据类型
export interface LoginResponse {
  token: string
  user: {
    id: number
    username: string
    realName: string
    email: string
    roleId: number
    companyId: number
    status: string
  }
  roles: string[]
}

// 认证管理API接口
export const authApi = {
  // 用户登录
  login(data: LoginRequest) {
    return request({
      url: '/api/auth/login',
      method: 'post',
      data
    })
  },

  // 用户登出
  logout() {
    return request({
      url: '/api/auth/logout',
      method: 'post'
    })
  },

  // 刷新token
  refreshToken() {
    return request({
      url: '/api/auth/refresh',
      method: 'post'
    })
  },

  // 获取当前用户信息
  getCurrentUser() {
    return request({
      url: '/api/auth/current-user',
      method: 'get'
    })
  },

  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return request({
      url: '/api/auth/change-password',
      method: 'post',
      data
    })
  },

  // 重置密码
  resetPassword(data: { username: string; email: string }) {
    return request({
      url: '/api/auth/reset-password',
      method: 'post',
      data
    })
  }
}

// 导出常用方法
export const {
  login,
  logout,
  refreshToken,
  getCurrentUser,
  changePassword,
  resetPassword
} = authApi
