import request from '@/utils/request'
import type { Customer } from '@/types/customer'

export const customerApi = {
  // 创建客户
  createCustomer(data: Customer): Promise<Customer> {
    return request.post('/api/customers', data)
  },

  // 更新客户
  updateCustomer(id: number, data: Customer): Promise<Customer> {
    return request.put(`/api/customers/${id}`, data)
  },

  // 删除客户
  deleteCustomer(id: number): Promise<boolean> {
    return request.delete(`/api/customers/${id}`)
  },

  // 根据ID获取客户
  getCustomerById(id: number): Promise<Customer> {
    return request.get(`/api/customers/${id}`)
  },

  // 根据客户编码获取客户
  getCustomerByCode(customerCode: string): Promise<Customer> {
    return request.get(`/api/customers/code/${customerCode}`)
  },

  // 获取所有客户
  getCustomerList(params?: any): Promise<Customer[]> {
    return request.get('/api/customers', { params })
  },

  // 根据公司ID获取客户列表
  getCustomersByCompanyId(companyId: number): Promise<Customer[]> {
    return request.get(`/api/customers/company/${companyId}`)
  },

  // 根据客户类型获取客户列表
  getCustomersByType(customerType: string): Promise<Customer[]> {
    return request.get(`/api/customers/type/${customerType}`)
  },

  // 根据客户等级获取客户列表
  getCustomersByLevel(customerLevel: string): Promise<Customer[]> {
    return request.get(`/api/customers/level/${customerLevel}`)
  },

  // 根据状态获取客户列表
  getCustomersByStatus(status: string): Promise<Customer[]> {
    return request.get(`/api/customers/status/${status}`)
  },

  // 根据关键词搜索客户
  searchCustomers(keyword: string): Promise<Customer[]> {
    return request.get('/api/customers/search', { params: { keyword } })
  }
}
