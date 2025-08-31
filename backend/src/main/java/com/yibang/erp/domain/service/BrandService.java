package com.yibang.erp.domain.service;

import com.yibang.erp.domain.entity.Brand;
import java.util.List;

/**
 * 品牌服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface BrandService {

    /**
     * 获取所有活跃品牌
     */
    List<Brand> getAllActiveBrands();

    /**
     * 根据ID获取品牌
     */
    Brand getBrandById(Long id);

    /**
     * 根据名称获取品牌
     */
    Brand getBrandByName(String name);

    /**
     * 创建品牌
     */
    Brand createBrand(Brand brand);

    /**
     * 更新品牌
     */
    Brand updateBrand(Brand brand);

    /**
     * 删除品牌
     */
    boolean deleteBrand(Long id);

    /**
     * 更新品牌状态
     */
    boolean updateBrandStatus(Long id, String status);
}
