import request from '@/utils/request'
import type { Company, CreateCompanyRequest, UpdateCompanyRequest, CompanyQueryParams } from '@/types/company'

// 公司管理API接口
export const companyApi = {
  // 分页获取公司列表
  getCompanyList(params: CompanyQueryParams) {
    return request({
      url: '/api/companies',
      method: 'get',
      params
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
  createCompany(data: CreateCompanyRequest) {
    return request({
      url: '/api/companies',
      method: 'post',
      data
    })
  },

  // 更新公司
  updateCompany(id: number, data: UpdateCompanyRequest) {
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
  },

  // 批量删除公司
  batchDeleteCompanies(ids: number[]) {
    return request({
      url: '/api/companies/batch',
      method: 'delete',
      data: ids
    })
  },

  // 更新公司状态
  updateCompanyStatus(id: number, status: string) {
    return request({
      url: `/api/companies/${id}/status`,
      method: 'patch',
      params: { status }
    })
  },

  // 获取所有公司列表
  getAllCompanies() {
    return request({
      url: '/api/companies/all',
      method: 'get'
    })
  },

  // 根据业务类型获取公司列表
  getCompaniesByType(type: string) {
    return request({
      url: `/api/companies/type/${type}`,
      method: 'get'
    })
  },

  // 获取激活状态的公司列表
  getActiveCompanies() {
    return request({
      url: '/api/companies/active',
      method: 'get'
    })
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
  getCompaniesByType,
  getActiveCompanies
} = companyApi
