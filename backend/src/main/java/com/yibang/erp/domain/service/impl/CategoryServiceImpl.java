package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yibang.erp.domain.entity.Category;
import com.yibang.erp.domain.service.CategoryService;
import com.yibang.erp.infrastructure.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllActiveCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, "ACTIVE")
               .orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getName);
        return categoryRepository.selectList(wrapper);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.selectById(id);
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // 设置默认值
        if (category.getLevel() == null) {
            category.setLevel(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus("ACTIVE");
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        categoryRepository.insert(category);
        return category;
    }

    @Override
    @Transactional
    public Category updateCategory(Category category) {
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.updateById(category);
        return category;
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        long childCount = categoryRepository.selectCount(wrapper);
        
        if (childCount > 0) {
            log.warn("无法删除分类，存在子分类，分类ID: {}", id);
            return false;
        }
        
        // 检查是否有关联的商品
        // TODO: 这里需要检查products表中是否有category_id = id的记录
        
        return categoryRepository.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean updateCategoryStatus(Long id, String status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.updateById(category) > 0;
    }

    @Override
    public List<Category> getCategoryTree() {
        // 获取所有活跃分类
        List<Category> allCategories = getAllActiveCategories();
        
        // 构建树结构
        return buildCategoryTree(allCategories, null);
    }

    @Override
    public List<Category> getCategoriesByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId)
               .eq(Category::getStatus, "ACTIVE")
               .orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getName);
        return categoryRepository.selectList(wrapper);
    }

    @Override
    public List<Category> getCategoriesByLevel(Integer level) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getLevel, level)
               .eq(Category::getStatus, "ACTIVE")
               .orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getName);
        return categoryRepository.selectList(wrapper);
    }

    /**
     * 构建分类树结构
     */
    private List<Category> buildCategoryTree(List<Category> allCategories, Long parentId) {
        return allCategories.stream()
                .filter(category -> 
                    (parentId == null && category.getParentId() == null) ||
                    (parentId != null && parentId.equals(category.getParentId()))
                )
                .collect(Collectors.toList());
    }
}
