package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色数据访问接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface RoleRepository extends BaseMapper<Role> {
}
