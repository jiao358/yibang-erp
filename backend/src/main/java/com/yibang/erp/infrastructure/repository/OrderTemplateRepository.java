package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.OrderTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单模板Repository接口
 */
@Mapper
public interface OrderTemplateRepository extends BaseMapper<OrderTemplate> {

    /**
     * 根据模板名称查询模板
     */
    @Select("SELECT * FROM order_templates WHERE template_name = #{templateName} AND is_deleted = 0 ORDER BY version DESC LIMIT 1")
    OrderTemplate selectByTemplateName(@Param("templateName") String templateName);

    /**
     * 根据模板名称和版本查询模板
     */
    @Select("SELECT * FROM order_templates WHERE template_name = #{templateName} AND version = #{version} AND is_deleted = 0")
    OrderTemplate selectByTemplateNameAndVersion(@Param("templateName") String templateName, 
                                               @Param("version") String version);

    /**
     * 根据是否启用查询模板列表
     */
    @Select("SELECT * FROM order_templates WHERE is_active = #{isActive} AND is_deleted = 0 ORDER BY created_at DESC")
    List<OrderTemplate> selectByIsActive(@Param("isActive") Boolean isActive);

    /**
     * 根据模板名称查询所有版本
     */
    @Select("SELECT * FROM order_templates WHERE template_name = #{templateName} AND is_deleted = 0 ORDER BY version DESC, created_at DESC")
    List<OrderTemplate> selectAllVersionsByTemplateName(@Param("templateName") String templateName);
}
