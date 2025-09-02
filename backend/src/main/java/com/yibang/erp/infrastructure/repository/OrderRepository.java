package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 订单Repository接口
 */
@Mapper
public interface OrderRepository extends BaseMapper<Order> {

    /**
     * 根据平台订单号查询订单
     */
    @Select("SELECT * FROM orders WHERE platform_order_id = #{platformOrderNo} AND deleted = 0")
    Order selectByPlatformOrderNo(@Param("platformOrderNo") String platformOrderNo);

    /**
     * 根据销售订单ID和销售用户ID查询订单
     */
    @Select("SELECT * FROM orders WHERE sales_order_id = #{salesOrderId} AND sales_id = #{salesUserId} AND deleted = 0")
    List<Order> selectBySalesOrderIdAndSalesUserId(@Param("salesOrderId") String salesOrderId, 
                                                   @Param("salesUserId") Long salesUserId);

    /**
     * 根据销售用户ID查询订单列表
     */
    @Select("SELECT * FROM orders WHERE sales_id = #{salesId} AND deleted = 0 ORDER BY created_at DESC")
    List<Order> selectBySalesUserId(@Param("salesId") Long salesId);

    /**
     * 根据供应商公司ID查询订单列表
     */
    @Select("SELECT * FROM orders WHERE supplier_company_id = #{supplierCompanyId} AND deleted = 0 ORDER BY created_at DESC")
    List<Order> selectBySupplierCompanyId(@Param("supplierCompanyId") Long supplierCompanyId);

    /**
     * 根据客户ID查询订单列表
     */
    @Select("SELECT * FROM orders WHERE customer_id = #{customerId} AND deleted = 0 ORDER BY created_at DESC")
    List<Order> selectByCustomerId(@Param("customerId") Long customerId);

    /**
     * 分页查询订单列表
     */
    IPage<Order> selectOrderPage(Page<Order> page, @Param("request") com.yibang.erp.domain.dto.OrderListRequest request);

    /**
     * 根据状态查询订单列表
     */
    @Select("SELECT * FROM orders WHERE order_status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<Order> selectByStatus(@Param("status") String status);

    /**
     * 根据来源查询订单列表
     */
    @Select("SELECT * FROM orders WHERE order_source = #{source} AND deleted = 0 ORDER BY created_at DESC")
    List<Order> selectBySource(@Param("source") String source);

    /**
     * 检查销售订单ID是否重复
     */
    @Select("SELECT COUNT(*) > 0 FROM orders WHERE sales_order_id = #{salesOrderId} AND sales_id = #{salesUserId} AND deleted = 0")
    boolean existsBySalesOrderIdAndSalesUserId(@Param("salesOrderId") String salesOrderId, 
                                              @Param("salesUserId") Long salesUserId);

    /**
     * 生成平台订单号
     */
    @Select("SELECT CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(COALESCE(MAX(SUBSTRING(platform_order_id, -4)), 0) + 1, 4, '0')) FROM orders WHERE platform_order_id LIKE CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    String generatePlatformOrderNo();
}
