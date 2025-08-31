package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类数据访问层
 */
@Mapper
public interface CategoryRepository extends BaseMapper<Category> {
}
