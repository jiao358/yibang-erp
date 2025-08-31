package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yibang.erp.domain.entity.Brand;
import com.yibang.erp.domain.service.BrandService;
import com.yibang.erp.infrastructure.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 品牌服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Override
    public List<Brand> getAllActiveBrands() {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE")
                   .eq("deleted", false)
                   .orderByAsc("name");
        return brandRepository.selectList(queryWrapper);
    }

    @Override
    public Brand getBrandById(Long id) {
        return brandRepository.selectById(id);
    }

    @Override
    public Brand getBrandByName(String name) {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name)
                   .eq("deleted", false);
        return brandRepository.selectOne(queryWrapper);
    }

    @Override
    public Brand createBrand(Brand brand) {
        brandRepository.insert(brand);
        return brand;
    }

    @Override
    public Brand updateBrand(Brand brand) {
        brandRepository.updateById(brand);
        return brand;
    }

    @Override
    public boolean deleteBrand(Long id) {
        return brandRepository.deleteById(id) > 0;
    }

    @Override
    public boolean updateBrandStatus(Long id, String status) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setStatus(status);
        return brandRepository.updateById(brand) > 0;
    }
}
