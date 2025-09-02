import request from '@/utils/request'
import type { Company, CreateCompanyRequest, UpdateCompanyRequest } from '@/types/company'

export const companyApi = {
  // 获取公司列表
  getCompanyList(params?: {
    page?: number
    size?: number
    name?: string
    type?: string
    status?: string
  }): Promise<any> {
    return request.get('/api/companies', { params })
  },

  // 根据ID获取公司
  getCompanyById(id: number): Promise<Company> {
    return request.get(`/api/companies/${id}`)
  },

  // 创建公司
  createCompany(data: CreateCompanyRequest): Promise<Company> {
    return request.post('/api/companies', data)
  },

  // 更新公司
  updateCompany(id: number, data: UpdateCompanyRequest): Promise<Company> {
    return request.put(`/api/companies/${id}`, data)
  },

  // 删除公司
  deleteCompany(id: number): Promise<void> {
    return request.delete(`/api/companies/${id}`)
  },

  // 获取可用公司列表（用于下拉选择）
  getAvailableCompanies(): Promise<{
    data: Company[]
    success: boolean
    message: string
    timestamp: number
  }> {
    return request.get('/api/users/companies/available')
  },

  // 获取供应商公司列表
  getSupplierCompanies() {
    return request.get('/api/companies/suppliers')
  }
}
