package com.yibang.erp.domain.service;

import com.yibang.erp.domain.entity.Category;
import java.util.List;

/**
 * 商品分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有活跃分类
     */
    List<Category> getAllActiveCategories();

    /**
     * 根据ID获取分类
     */
    Category getCategoryById(Long id);

    /**
     * 创建分类
     */
    Category createCategory(Category category);

    /**
     * 更新分类
     */
    Category updateCategory(Category category);

    /**
     * 删除分类
     */
    boolean deleteCategory(Long id);

    /**
     * 更新分类状态
     */
    boolean updateCategoryStatus(Long id, String status);

    /**
     * 获取分类树结构
     */
    List<Category> getCategoryTree();

    /**
     * 根据父分类ID获取子分类
     */
    List<Category> getCategoriesByParentId(Long parentId);

    /**
     * 根据层级获取分类
     */
    List<Category> getCategoriesByLevel(Integer level);
}
