package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.*;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {


    /**
     * 处理订单关闭消息（通过HSF）
     * @param sourceOrderId
     * @return
     */
    OrderCloseMessageResponse handleCloseOrderHsf(String sourceOrderId);


    /**
     *
     * 处理订单状态变更消息
     * @param message
     * @return
     */
    OrderCloseMessageResponse handleCloseOrderMessage(OrderStatusChangeMessage message);
    /**
     *
     * @param message
     * @return
     */

    AddressChangeOrderResponse handleAddressChangeMessage(AddressChangeMessage message);


    /**
     * 创建订单（通过API）
     */
    public OrderResponse createOrderByAPI(OrderCreateRequest request) ;

    /**
     * 创建订单
     */
    OrderResponse createOrder(OrderCreateRequest request);


    OrderResponse createOrderByHsf(OrderCreateRequest request);

    /**
     * 更新订单
     */
    OrderResponse updateOrder(Long orderId, OrderUpdateRequest request);

    /**
     * 删除订单
     */
    boolean deleteOrder(Long orderId);

    /**
     * 根据ID获取订单
     */
    OrderResponse getOrderById(Long orderId);

    /**
     * 根据平台订单号获取订单
     */
    OrderResponse getOrderByPlatformOrderNo(String platformOrderNo);

    /**
     * 创建订单（通过API）
     */
    public String generatePlatformOrderNoForAPI(String userName);
    /**
     * 分页查询订单列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderResponse> getOrderList(OrderListRequest request);

    com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderResponse> getOrderSalesList(OrderListRequest request);

    com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderResponse> getOrderSuplierList(OrderListRequest request);

    /**
     * 更新订单状态
     */
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);

    /**
     * 提交订单（草稿 -> 已提交）
     */
    OrderResponse submitOrder(Long orderId);

    /**
     * 取消订单
     */
    OrderResponse cancelOrder(Long orderId);

    /**
     * 供应商确认订单
     */
    OrderResponse supplierConfirmOrder(Long orderId);

    /**
     * 供应商发货
     */
    OrderResponse supplierShipOrder(Long orderId, SupplierShipRequest request);

    /**
     * 供应商拒绝订单
     */
    OrderResponse supplierRejectOrder(Long orderId, SupplierRejectRequest request);

    /**
     * 已结清订单
     */
    OrderResponse completeOrder(Long orderId);

    /**
     * 处理订单冲突
     */
    OrderResponse resolveOrderConflict(Long orderId, OrderConflictResolutionRequest request);

    /**
     * 批量供应商确认订单
     */
    OrderBatchConfirmResponse batchSupplierConfirmOrders(OrderBatchConfirmRequest request);

    /**
     * 批量处理订单
     */
    OrderBatchProcessResponse batchProcessOrders(OrderBatchProcessRequest request);

    /**
     * 根据销售用户ID获取订单列表
     */
    List<OrderResponse> getOrdersBySalesUserId(Long salesUserId);

    /**
     * 根据供应商公司ID获取订单列表
     */
    List<OrderResponse> getOrdersBySupplierCompanyId(Long supplierCompanyId);

    /**
     * 根据客户ID获取订单列表
     */
    List<OrderResponse> getOrdersByCustomerId(Long customerId);

    /**
     * 检查订单状态转换是否有效
     */
    boolean isValidStatusTransition(String currentStatus, String targetStatus);

    /**
     * 获取订单状态转换历史
     */
    List<com.yibang.erp.domain.entity.OrderStatusLog> getOrderStatusHistory(Long orderId);

    /**
     * 生成平台订单号
     */
    String generatePlatformOrderNo();

    /**
     * 导出订单数据
     */
    byte[] exportOrders(List<Long> orderIds);

    /**
     * 下载发货模板
     */
    byte[] downloadShipTemplate();

    /**
     * 预览发货导入数据
     */
    ShipImportPreviewResponse previewShipImport(byte[] fileData);

    /**
     * 导入发货数据
     */
    ShipImportResultResponse importShipData(byte[] fileData);

    /**
     * 生成平台订单号（指定账户和渠道）
     */
    String generatePlatformOrderNo(String userName, String orderSource);

    /**
     * 批量预生成订单号
     */
    List<String> preGenerateOrderNumbers(String userName, String orderSource, int count);

    /**
     * 验证订单数据完整性
     */
    boolean validateOrderData(OrderCreateRequest request);

    /**
     * 处理多渠道订单冲突
     */
    OrderResponse handleMultiSourceConflict(String salesOrderId, Long salesUserId, List<OrderCreateRequest> conflictingOrders);

    /**
     * 根据源订单号获取订单
     */
    com.yibang.erp.domain.entity.Order getOrderBySourceOrderId(String sourceOrderId);
}
