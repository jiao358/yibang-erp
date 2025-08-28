package com.yibang.erp.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Company;

import java.util.List;

/**
 * 公司服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface CompanyService {

    /**
     * 分页查询公司列表
     *
     * @param page 分页参数
     * @param name 公司名称（可选）
     * @param type 业务类型（可选）
     * @param status 状态（可选）
     * @return 分页公司列表
     */
    IPage<Company> getCompanyPage(Page<Company> page, String name, String type, String status);

    /**
     * 根据ID获取公司
     *
     * @param id 公司ID
     * @return 公司信息
     */
    Company getCompanyById(Long id);

    /**
     * 根据名称获取公司
     *
     * @param name 公司名称
     * @return 公司信息
     */
    Company getCompanyByName(String name);

    /**
     * 创建公司
     *
     * @param company 公司信息
     * @return 创建后的公司
     */
    Company createCompany(Company company);

    /**
     * 更新公司
     *
     * @param company 公司信息
     * @return 更新后的公司
     */
    Company updateCompany(Company company);

    /**
     * 删除公司
     *
     * @param id 公司ID
     * @return 是否删除成功
     */
    boolean deleteCompany(Long id);

    /**
     * 批量删除公司
     *
     * @param ids 公司ID列表
     * @return 删除成功的数量
     */
    int batchDeleteCompanies(List<Long> ids);

    /**
     * 更新公司状态
     *
     * @param id 公司ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateCompanyStatus(Long id, String status);

    /**
     * 获取所有公司列表
     *
     * @return 公司列表
     */
    List<Company> getAllCompanies();

    /**
     * 根据业务类型获取公司列表
     *
     * @param type 业务类型
     * @return 公司列表
     */
    List<Company> getCompaniesByType(String type);

    /**
     * 获取激活状态的公司列表
     *
     * @return 激活的公司列表
     */
    List<Company> getActiveCompanies();
}
