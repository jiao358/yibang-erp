package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Company;
import com.yibang.erp.domain.service.CompanyService;
import com.yibang.erp.infrastructure.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 公司服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public IPage<Company> getCompanyPage(Page<Company> page, String name, String type, String status) {


        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Company::getName, name);
        }
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Company::getType, type);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Company::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Company::getCreatedAt);
        //增加count
        Long totalCount= companyRepository.selectCount(queryWrapper);
        page.setTotal(totalCount);
        return companyRepository.selectPage(page, queryWrapper);
    }

    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.selectById(id);
    }

    @Override
    public Company getCompanyByName(String name) {
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Company::getName, name);
        return companyRepository.selectOne(queryWrapper);
    }

    @Override
    public Company createCompany(Company company) {
        // 检查公司名称是否已存在
        if (getCompanyByName(company.getName()) != null) {
            throw new RuntimeException("公司名称已存在");
        }
        
        // 设置默认值
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
        company.setDeleted(false);
        
        // 保存公司
        companyRepository.insert(company);
        return company;
    }

    @Override
    public Company updateCompany(Company company) {
        // 检查公司是否存在
        Company existingCompany = getCompanyById(company.getId());
        if (existingCompany == null) {
            throw new RuntimeException("公司不存在");
        }
        
        // 检查公司名称是否重复（排除自己）
        Company companyWithSameName = getCompanyByName(company.getName());
        if (companyWithSameName != null && !companyWithSameName.getId().equals(company.getId())) {
            throw new RuntimeException("公司名称已存在");
        }
        
        // 设置更新时间
        company.setUpdatedAt(LocalDateTime.now());
        
        // 更新公司
        companyRepository.updateById(company);
        return getCompanyById(company.getId());
    }

    @Override
    public boolean deleteCompany(Long id) {
        return companyRepository.deleteById(id) > 0;
    }

    @Override
    public int batchDeleteCompanies(List<Long> ids) {
        return companyRepository.deleteBatchIds(ids);
    }

    @Override
    public boolean updateCompanyStatus(Long id, String status) {
        Company company = new Company();
        company.setId(id);
        company.setStatus(status);
        company.setUpdatedAt(LocalDateTime.now());
        
        return companyRepository.updateById(company) > 0;
    }

    @Override
    public List<Company> getAllCompanies() {
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Company::getCreatedAt);
        return companyRepository.selectList(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getAllActiveCompanies() {
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Company::getStatus, "ACTIVE")
                   .eq(Company::getDeleted, false)
                   .orderByAsc(Company::getName);
        
        List<Company> companies = companyRepository.selectList(queryWrapper);
        return companies.stream()
            .map(company -> {
                Map<String, Object> companyMap = new HashMap<>();
                companyMap.put("id", company.getId());
                companyMap.put("name", company.getName());
                companyMap.put("type", company.getType());
                companyMap.put("parentSupplierId", company.getParentSupplierId());
                companyMap.put("supplierLevel", company.getSupplierLevel());
                companyMap.put("status", company.getStatus());
                return companyMap;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCompaniesByTypeMap(String type) {
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Company::getType, type)
                   .eq(Company::getStatus, "ACTIVE")
                   .eq(Company::getDeleted, false)
                   .orderByAsc(Company::getName);
        
        List<Company> companies = companyRepository.selectList(queryWrapper);
        return companies.stream()
            .map(company -> {
                Map<String, Object> companyMap = new HashMap<>();
                companyMap.put("id", company.getId());
                companyMap.put("name", company.getName());
                companyMap.put("type", company.getType());
                companyMap.put("parentSupplierId", company.getParentSupplierId());
                companyMap.put("supplierLevel", company.getSupplierLevel());
                companyMap.put("status", company.getStatus());
                return companyMap;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<Company> getCompaniesByType(String type) {
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Company::getType, type);
        queryWrapper.eq(Company::getStatus, "ACTIVE");
        queryWrapper.orderByAsc(Company::getName);
        return companyRepository.selectList(queryWrapper);
    }

    @Override
    public List<Company> getActiveCompanies() {
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Company::getStatus, "ACTIVE");
        queryWrapper.orderByAsc(Company::getName);
        return companyRepository.selectList(queryWrapper);
    }
}
