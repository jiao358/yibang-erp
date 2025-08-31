package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.CompanySupplierRelationship;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应链公司与销售公司关系数据访问层
 *
 * @author yibang-erp
 * @since 2025-08-29
 */
@Mapper
public interface CompanySupplierRelationshipRepository extends BaseMapper<CompanySupplierRelationship> {
}
