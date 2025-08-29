import request from '@/utils/request'
import type { Company, CompanyForm } from '@/types/company'

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
  createCompany(data: CompanyForm) {
    return request.post('/api/companies', data)
  },

  // 更新公司
  updateCompany(id: number, data: CompanyForm) {
    return request.put(`/api/companies/${id}`, data)
  },

  // 删除公司
  deleteCompany(id: number) {
    return request.delete(`/api/companies/${id}`)
  },

  // 批量删除公司
  batchDeleteCompanies(ids: number[]) {
    return request.delete('/api/companies/batch', { data: ids })
  },

  // 更新公司状态
  updateCompanyStatus(id: number, status: string) {
    return request.patch(`/api/companies/${id}/status`, null, { params: { status } })
  },

  // 获取所有公司
  getAllCompanies() {
    return request.get('/api/companies/all')
  },

  // 获取活跃公司
  getActiveCompanies() {
    return request.get('/api/companies/active')
  }
}

// 导出常用方法
export const {
  getCompanyList,
  getCompanyById,
  createCompany,
  updateCompany,
  deleteCompany,
  batchDeleteCompanies,
  updateCompanyStatus,
  getAllCompanies,
  getActiveCompanies
} = companyApi
