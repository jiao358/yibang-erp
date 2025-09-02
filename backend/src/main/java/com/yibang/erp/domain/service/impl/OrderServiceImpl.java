package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yibang.erp.domain.dto.*;
import com.yibang.erp.domain.entity.*;
import com.yibang.erp.domain.service.OrderNumberGeneratorService;
import com.yibang.erp.domain.service.OrderService;
import com.yibang.erp.infrastructure.repository.*;
import com.yibang.erp.infrastructure.client.DeepSeekClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl extends ServiceImpl<OrderRepository, Order> implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderStatusLogRepository orderStatusLogRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private OrderNumberGeneratorService orderNumberGeneratorService;

    @Autowired
    private DeepSeekClient deepSeekClient;

    private OrderAddressDTO aiModelHandleAddress(String address){
        try {
            // 检查AI功能是否启用
            if (address == null || address.trim().isEmpty()) {
                return createDefaultAddressDTO();
            }
            
            // 调用DeepSeek API进行地址解析
            String aiResult = deepSeekClient.parseAddress(address.trim()).block();
            
            if (aiResult == null || aiResult.contains("AI地址解析失败")) {
                log.warn("AI地址解析失败，使用默认处理: {}", address);
//                throw new RuntimeException("AI地址解析失败，请检查配置或稍后重试");
                return createDefaultAddressDTO();
            }
            
            // 解析AI返回的JSON结果
            OrderAddressDTO addressDTO = parseAIAddressResult(aiResult);
            
            // 如果解析失败，使用默认处理
            if (addressDTO == null) {
                log.warn("AI地址解析结果解析失败，使用默认处理: {}", aiResult);
                return createDefaultAddressDTO();
            }
            
            log.info("AI地址解析成功: {} -> {}", address, addressDTO);
            return addressDTO;
            
        } catch (Exception e) {
            log.error("AI地址解析异常: {}", e.getMessage(), e);
            return createDefaultAddressDTO();
        }
    }
    
    /**
     * 解析AI返回的地址结果
     */
    private OrderAddressDTO parseAIAddressResult(String aiResult) {
        try {
            // 尝试解析JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(aiResult);
            
            OrderAddressDTO addressDTO = new OrderAddressDTO();
            addressDTO.setProvinceName(getJsonValue(jsonNode, "provinceName"));
            addressDTO.setCityName(getJsonValue(jsonNode, "cityName"));
            addressDTO.setDistrictName(getJsonValue(jsonNode, "districtName"));
            addressDTO.setFixAddress(getJsonValue(jsonNode, "fixAddress"));
            
            // 解析置信度
            JsonNode confidenceNode = jsonNode.get("confidence");
            if (confidenceNode != null && !confidenceNode.isNull()) {
                addressDTO.setConfidence(confidenceNode.asDouble());
            } else {
                addressDTO.setConfidence(0.0);
            }
            
            return addressDTO;
            
        } catch (Exception e) {
            log.error("解析AI地址结果失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取JSON字段值
     */
    private String getJsonValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        if (node != null && !node.isNull()) {
            return node.asText();
        }
        return null;
    }
    
    /**
     * 创建默认地址DTO
     */
    private OrderAddressDTO createDefaultAddressDTO() {
        OrderAddressDTO addressDTO = new OrderAddressDTO();
        addressDTO.setProvinceName(null);
        addressDTO.setCityName(null);
        addressDTO.setDistrictName(null);
        addressDTO.setConfidence(0.0);
        addressDTO.setFixAddress(null);
        return addressDTO;
    }

    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 验证请求数据
        validateOrderRequest(request);

        //说明肯定有品

        //todo 如果是两家供应链甚至是更多供应链商品的组合，那么就应该生成多个分发的订单。现在因为只有一个供应链，所以无碍查询一个商品的公司即可

        Product product = productRepository.selectById(request.getOrderItems().get(0).getProductId());
        Long supplierCompanyId = product.getCompanyId();

        // 创建订单
        Order order = new Order();
        order.setPlatformOrderId(generatePlatformOrderNo());
        order.setCustomerId(request.getCustomerId());
        order.setSalesId(request.getSalesUserId());
        /**
         * todo 这里就有问题了
         * 销售公司可能是一棒 而不是供应链公司
         * 供应链公司的逻辑，应该是从品上看的
         */
        order.setSupplierCompanyId(supplierCompanyId);

        order.setOrderType("NORMAL"); // 默认订单类型
        order.setOrderSource(request.getSource());
        order.setOrderStatus("DRAFT");
        if(request.getExtendedFields().get("expectedDeliveryDate")!=null){
            order.setExpectedDeliveryDate(LocalDate.parse(request.getExtendedFields().get("expectedDeliveryDate").toString()));
        }else{
            order.setExpectedDeliveryDate(LocalDate.now().plusDays(7)); // 默认7天后
        }

        order.setCurrency(request.getExtendedFields().get("currency").toString()); // 默认人民币
        order.setSpecialRequirements(request.getRemarks());
        String address = request.getExtendedFields().get("deliveryAddress").toString();

        // 使用AI自动拆分省份、城市、区域、详细地址
        OrderAddressDTO orderAddressDTO = aiModelHandleAddress(address);
        
        // 设置地址相关字段
        order.setDeliveryAddress(request.getExtendedFields().get("deliveryAddress").toString());
        order.setDeliveryContact(request.getExtendedFields().get("deliveryContact").toString());
        
        // 设置AI解析的地址信息
        if (orderAddressDTO != null && orderAddressDTO.getConfidence() > 0) {
            order.setProvinceName(orderAddressDTO.getProvinceName());
            order.setCityName(orderAddressDTO.getCityName());
            order.setDistrictName(orderAddressDTO.getDistrictName());
            order.setAiConfidence(BigDecimal.valueOf(orderAddressDTO.getConfidence()));
            order.setAiProcessed(true);
        } else {
            order.setAiProcessed(false);
            order.setAiConfidence(BigDecimal.ZERO);
        }




        order.setDeliveryPhone(request.getExtendedFields().get("deliveryPhone").toString());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setDiscountAmount(request.getExtendedFields().get("discountAmount") != null ? new BigDecimal(request.getExtendedFields().get("discountAmount").toString()) : BigDecimal.ZERO);

        order.setShippingAmount(request.getExtendedFields().get("shippingAmount") != null ? new BigDecimal(request.getExtendedFields().get("shippingAmount").toString()) : BigDecimal.ZERO);

        order.setTaxAmount(request.getExtendedFields().get("taxAmount") != null ? new BigDecimal(request.getExtendedFields().get("taxAmount").toString()) : BigDecimal.ZERO);

        order.setFinalAmount(BigDecimal.ZERO);


        order.setPaymentMethod(request.getExtendedFields().get("paymentMethod").toString());
        order.setPaymentStatus("UNPAID");
        order.setApprovalStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setCreatedBy(request.getSalesUserId());
        order.setUpdatedBy(request.getSalesUserId());

        // 处理扩展字段
        if (request.getExtendedFields() != null) {
            order.setExtendedFieldsMap(request.getExtendedFields());
        }

        // 保存订单
        orderRepository.insert(order);

        // 创建订单项
        if (request.getOrderItems() != null && !request.getOrderItems().isEmpty()) {
            createOrderItems(order.getId(), request.getOrderItems());
        }


        // 计算订单总金额
        calculateOrderTotal(order.getId());

        return getOrderById(order.getId());
    }

    @Override
    public OrderResponse updateOrder(Long orderId, OrderUpdateRequest request) {
        Order order = getOrderEntityById(orderId);

        // 检查订单状态是否可以修改
        if (!canModifyOrder(order)) {
            throw new IllegalStateException("订单状态不允许修改");
        }

        // 更新订单基本信息
        if (request.getCustomerId() != null) {
            order.setCustomerId(request.getCustomerId());
        }


        if (request.getRemarks() != null) {
            order.setRemarks(request.getRemarks());
        }
        if (request.getExtendedFields() != null) {
            order.setExtendedFieldsMap(request.getExtendedFields());
        }

        boolean isEdit= false;
        //todo 增加省份 城市 区域 的修改
        if (request.getExtendedFields().get("provinceName") != null) {
            order.setProvinceName(request.getExtendedFields().get("provinceName").toString());
            isEdit=isEdit||true;
        }
        if(request.getExtendedFields().get("cityName") != null){
            order.setCityName(request.getExtendedFields().get("cityName").toString());
            isEdit=isEdit||true;
        }

        if(request.getExtendedFields().get("districtName") != null){
            order.setDistrictName(request.getExtendedFields().get("districtName").toString());
            isEdit=isEdit||true;
        }

        //如果有修改 ！ 则被证明是人工确认的 ai—process要成为false
        if(isEdit){
            order.setAiProcessed(false);
            order.setAiConfidence(BigDecimal.ZERO);
        }

        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(getCurrentUserId());
        orderRepository.updateById(order);

        // 更新订单项
        if (request.getOrderItems() != null) {
            updateOrderItems(orderId, request.getOrderItems());
        }

        // 重新计算订单总金额
        calculateOrderTotal(orderId);

        return getOrderById(orderId);
    }

    @Override
    public boolean deleteOrder(Long orderId) {
        Order order = getOrderEntityById(orderId);

        // 检查订单状态是否可以删除
        if (!canDeleteOrder(order)) {
            throw new IllegalStateException("订单状态不允许删除");
        }

        // 删除订单项
        QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", orderId);
        orderItemRepository.delete(itemWrapper);

        // 删除订单状态日志
        QueryWrapper<OrderStatusLog> logWrapper = new QueryWrapper<>();
        logWrapper.eq("order_id", orderId);
        orderStatusLogRepository.delete(logWrapper);

        // 删除订单
        return orderRepository.deleteById(orderId) > 0;
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = getOrderEntityById(orderId);
        return enrichOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderByPlatformOrderNo(String platformOrderNo) {
        Order order = orderRepository.selectByPlatformOrderNo(platformOrderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + platformOrderNo);
        }
        return enrichOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrderList(OrderListRequest request) {
        // 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();

        if(org.apache.commons.lang3.StringUtils.isNotEmpty(request.getPlatformOrderNo())){
            queryWrapper.eq("platform_order_id", request.getPlatformOrderNo());
        }
        
        if (request.getCustomerId() != null) {
            queryWrapper.eq("customer_id", request.getCustomerId());
        }
        
        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq("order_status", request.getStatus());
        }
        
        if (StringUtils.hasText(request.getSource())) {
            queryWrapper.eq("order_source", request.getSource());
        }
        
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                .like("platform_order_id", request.getKeyword())
                .or()
                .like("special_requirements", request.getKeyword())
            );
        }
        
        queryWrapper.orderByDesc("created_at");

        // 执行分页查询
        Page<Order> page = new Page<>(request.getCurrent(), request.getSize());
        Page<Order> orderPage = orderRepository.selectPage(page, queryWrapper);
        long count= orderRepository.selectCount(queryWrapper);
        // 转换为响应对象
        Page<OrderResponse> responsePage = new Page<>();
        responsePage.setCurrent(orderPage.getCurrent());
        responsePage.setSize(orderPage.getSize());
        responsePage.setTotal(count);
        responsePage.setPages(orderPage.getPages());

        List<OrderResponse> responses = orderPage.getRecords().stream()
                .map(this::enrichOrderResponse)
                .toList();
        responsePage.setRecords(responses);

        return responsePage;
    }

    @Override
    public Page<OrderResponse> getOrderSalesList(OrderListRequest request) {
        // 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(request.getPlatformOrderNo())){
            queryWrapper.eq("platform_order_id", request.getPlatformOrderNo());
        }
        //只有customerName  就当收件人 不是客户！
        if (org.apache.commons.lang3.StringUtils.isNoneEmpty(request.getCustomerName()) ) {
            queryWrapper.like("delivery_contact", request.getCustomerName());
        }

        if(request.getSalesUserId()!=null){
            queryWrapper.eq("sales_id", request.getSalesUserId());
        }

        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq("order_status", request.getStatus());
        }

        if (StringUtils.hasText(request.getSource())) {
            queryWrapper.eq("order_source", request.getSource());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("platform_order_id", request.getKeyword())
                    .or()
                    .like("special_requirements", request.getKeyword())
            );
        }

        queryWrapper.orderByDesc("created_at");

        // 执行分页查询
        Page<Order> page = new Page<>(request.getCurrent(), request.getSize());
        Page<Order> orderPage = orderRepository.selectPage(page, queryWrapper);

        long count= orderRepository.selectCount(queryWrapper);
        // 转换为响应对象
        Page<OrderResponse> responsePage = new Page<>();
        responsePage.setCurrent(orderPage.getCurrent());
        responsePage.setSize(orderPage.getSize());
        responsePage.setTotal(count);
        responsePage.setPages(orderPage.getPages());

        List<OrderResponse> responses = orderPage.getRecords().stream()
                .map(this::enrichOrderResponse)
                .toList();
        responsePage.setRecords(responses);

        return responsePage;

    }

    @Override
    public Page<OrderResponse> getOrderSuplierList(OrderListRequest request) {
        // 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();


        if(org.apache.commons.lang3.StringUtils.isNotEmpty(request.getPlatformOrderNo())){
            queryWrapper.eq("platform_order_id", request.getPlatformOrderNo());
        }
        //只有customerName  就当收件人 不是客户！
        if (org.apache.commons.lang3.StringUtils.isNoneEmpty(request.getCustomerName()) ) {
            queryWrapper.like("delivery_contact", request.getCustomerName());
        }

        if(request.getSupplierCompanyId()!=null){
            queryWrapper.eq("supplier_company_id", request.getSupplierCompanyId());
            /**
             * 供应商只能看到推送到自己的订单 提交、审核、发货、处理、拒绝、送达、完成
             */
            queryWrapper.in("order_status", "SUBMITTED","APPROVED","SHIPPED","PROCESSING","DELIVERED","COMPLETED");
        }

        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq("order_status", request.getStatus());
        }
        /**
         * 手动，excel，图片，api
         */

        if (StringUtils.hasText(request.getSource())) {
            queryWrapper.eq("order_source", request.getSource());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("platform_order_id", request.getKeyword())
                    .or()
                    .like("special_requirements", request.getKeyword())
            );
        }

        queryWrapper.orderByDesc("created_at");

        // 执行分页查询
        Page<Order> page = new Page<>(request.getCurrent(), request.getSize());
        Page<Order> orderPage = orderRepository.selectPage(page, queryWrapper);
        long count= orderRepository.selectCount(queryWrapper);
        // 转换为响应对象
        Page<OrderResponse> responsePage = new Page<>();
        responsePage.setCurrent(orderPage.getCurrent());
        responsePage.setSize(orderPage.getSize());
        responsePage.setTotal(count);
        responsePage.setPages(orderPage.getPages());

        List<OrderResponse> responses = orderPage.getRecords().stream()
                .map(this::enrichOrderResponse)
                .toList();
        responsePage.setRecords(responses);

        return responsePage;
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = getOrderEntityById(orderId);

        // 验证状态转换
        if (!isValidStatusTransition(order.getOrderStatus(), request.getTargetStatus())) {
            throw new IllegalStateException("无效的状态转换: " + order.getOrderStatus() + " -> " + request.getTargetStatus());
        }

        // 记录状态变更日志
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(order.getOrderStatus());
        log.setToStatus(request.getTargetStatus());
        log.setChangeReason(request.getReason());
        log.setOperatorId(request.getOperatorId());
        log.setOperatorType("USER");
        log.setCreatedAt(LocalDateTime.now());
        orderStatusLogRepository.insert(log);

        // 更新订单状态
        order.setOrderStatus(request.getTargetStatus());
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(request.getOperatorId());
        orderRepository.updateById(order);

        return getOrderById(orderId);
    }

    @Override
    public OrderResponse submitOrder(Long orderId) {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        //todo 如果订单处于提交状态或者拒绝状态，则不允许提交，只有CANCELLED、DRAFT 的状态才可以提交





        request.setTargetStatus("SUBMITTED");
        request.setReason("订单提交");
        request.setOperatorId(getCurrentUserId());
        return updateOrderStatus(orderId, request);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();

        //todo 对销售员来说 只有DRAFT、SUBMITTED 状态的订单才可以取消
        request.setTargetStatus("CANCELLED");
        request.setReason("订单取消");
        request.setOperatorId(getCurrentUserId());
        return updateOrderStatus(orderId, request);
    }

    @Override
    public OrderResponse supplierConfirmOrder(Long orderId) {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        //todo 只有供应商才能操作，并且订单必须为SUBMITTED的状态才可以
        request.setTargetStatus("APPROVED");
        request.setReason("供应商确认");
        request.setOperatorId(getCurrentUserId());
        return updateOrderStatus(orderId, request);
    }

    @Override
    public OrderResponse supplierShipOrder(Long orderId) {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        //todo 只有供应商才能操作，并且订单必须为Approved 才可以发货
        request.setTargetStatus("SHIPPED");
        request.setReason("供应商发货");
        request.setOperatorId(getCurrentUserId());
        return updateOrderStatus(orderId, request);
    }

    @Override
    public OrderResponse resolveOrderConflict(Long orderId, OrderConflictResolutionRequest request) {
        Order order = getOrderEntityById(orderId);

        // 处理冲突解决逻辑
        if ("ACCEPT_SALES".equals(request.getResolutionType())) {
            // 接受销售订单版本
            order.setOrderStatus("APPROVED");
            order.setRemarks("冲突已解决，接受销售订单版本");
        } else if ("ACCEPT_SUPPLIER".equals(request.getResolutionType())) {
            // 接受供应商版本
            order.setOrderStatus("APPROVED");
            order.setRemarks("冲突已解决，接受供应商版本");
        } else if ("MANUAL_REVIEW".equals(request.getResolutionType())) {
            // 需要人工审核
            order.setOrderStatus("PENDING_APPROVAL");
            order.setRemarks("冲突需要人工审核: " + request.getReason());
        }

        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(getCurrentUserId());
        orderRepository.updateById(order);

        return getOrderById(orderId);
    }

    @Override
    public OrderBatchProcessResponse batchProcessOrders(OrderBatchProcessRequest request) {
        // TODO: 实现批量处理逻辑
        throw new UnsupportedOperationException("批量处理功能待实现");
    }

    @Override
    public List<OrderResponse> getOrdersBySalesUserId(Long salesUserId) {
        List<Order> orders = orderRepository.selectBySalesUserId(salesUserId);
        return orders.stream()
                .map(this::enrichOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getOrdersBySupplierCompanyId(Long supplierCompanyId) {
        List<Order> orders = orderRepository.selectBySupplierCompanyId(supplierCompanyId);
        return orders.stream()
                .map(this::enrichOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.selectByCustomerId(customerId);
        return orders.stream()
                .map(this::enrichOrderResponse)
                .toList();
    }

    @Override
    public boolean isValidStatusTransition(String currentStatus, String targetStatus) {
        // 定义有效的状态转换规则
        return switch (currentStatus) {
            case "DRAFT" -> List.of("SUBMITTED", "CANCELLED").contains(targetStatus);
            case "SUBMITTED" -> List.of("APPROVED", "CANCELLED","REJECTED").contains(targetStatus);
            case "PENDING_APPROVAL" -> List.of("APPROVED", "REJECTED").contains(targetStatus);
            case "APPROVED" -> List.of("SHIPPED", "REJECTED","CANCELLED").contains(targetStatus);
            case "PROCESSING" -> List.of("SHIPPED", "CANCELLED").contains(targetStatus);
            case "SHIPPED" -> List.of("DELIVERED", "COMPLETED").contains(targetStatus);
            case "DELIVERED" -> List.of("COMPLETED").contains(targetStatus);
            default -> false;
        };
    }

    @Override
    public List<OrderStatusLog> getOrderStatusHistory(Long orderId) {
        return orderStatusLogRepository.selectByOrderId(orderId);
    }

    @Override
    public String generatePlatformOrderNo() {
        // 使用新的订单号生成服务，默认使用当前登录用户ID和手动创建渠道
        // 这里需要从SecurityContext获取当前用户信息
//        Long currentUserId = getCurrentUserId();
        String orderSource = "MANUAL";
        // 使用登录用户ID作为账户号
//        Long accountId = currentUserId;

        UserDetails userDetails=((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        String userName = userDetails.getUsername();

        
        return orderNumberGeneratorService.generatePlatformOrderNo(userName, orderSource);
    }

    @Override
    public String generatePlatformOrderNo(String userName, String orderSource) {
        return orderNumberGeneratorService.generatePlatformOrderNo(userName, orderSource);
    }

    @Override
    public List<String> preGenerateOrderNumbers(String userName, String orderSource, int count) {
        return orderNumberGeneratorService.preGenerateOrderNumbers(userName, orderSource, count);
    }

    @Override
    public boolean validateOrderData(OrderCreateRequest request) {
        // 基本验证
        if (request.getCustomerId() == null) {
            return false;
        }
        if (StringUtils.isEmpty(request.getSource())) {
            return false;
        }
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            return false;
        }

        // 验证订单项
        for (OrderCreateRequest.OrderItemRequest item : request.getOrderItems()) {
            if (item.getProductId() == null || item.getQuantity() <= 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public OrderResponse handleMultiSourceConflict(String salesOrderId, Long salesUserId, List<OrderCreateRequest> conflictingOrders) {
        // TODO: 实现多渠道冲突处理逻辑
        throw new UnsupportedOperationException("多渠道冲突处理功能待实现");
    }

    // 私有方法

    private void validateOrderRequest(OrderCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (!validateOrderData(request)) {
            throw new IllegalArgumentException("订单数据验证失败");
        }
    }

    private Order getOrderEntityById(Long orderId) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }
        return order;
    }

    private boolean canModifyOrder(Order order) {
        return List.of("DRAFT", "SUBMITTED", "PENDING_APPROVAL").contains(order.getOrderStatus());
    }

    private boolean canDeleteOrder(Order order) {
        return List.of("DRAFT", "CANCELLED").contains(order.getOrderStatus());
    }

    private void createOrderItems(Long orderId, List<OrderCreateRequest.OrderItemRequest> itemRequests) {
        for (OrderCreateRequest.OrderItemRequest itemRequest : itemRequests) {
            OrderItem orderItem = new OrderItem();
            if(itemRequest.getProductId()==null ){
                continue;
            }
            orderItem.setOrderId(orderId);
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(itemRequest.getUnitPrice());
            orderItem.setUnit(itemRequest.getExtendedFields().get("unit").toString()); // 默认单位

            orderItem.setDiscountRate(BigDecimal.ONE);
            orderItem.setDiscountAmount(BigDecimal.ZERO);
            orderItem.setTaxRate(BigDecimal.ZERO);
            orderItem.setTaxAmount(BigDecimal.ZERO);


            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());

            // 获取产品信息
            Product product = productRepository.selectById(itemRequest.getProductId());
            if (product != null) {
                orderItem.setSku(product.getSku());
                orderItem.setProductName(product.getName());
            }

            // 计算小计
            orderItem.calculateTotalPrice();

            orderItemRepository.insert(orderItem);
        }
    }

    private void updateOrderItems(Long orderId, List<OrderUpdateRequest.OrderItemUpdateRequest> itemRequests) {
        // 删除现有订单项
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        orderItemRepository.delete(wrapper);

        // 创建新的订单项
        for (OrderUpdateRequest.OrderItemUpdateRequest itemRequest : itemRequests) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(itemRequest.getUnitPrice());
            orderItem.setUnit("个"); // 默认单位
            orderItem.setDiscountRate(BigDecimal.ONE);
            orderItem.setDiscountAmount(BigDecimal.ZERO);
            orderItem.setTaxRate(BigDecimal.ZERO);
            orderItem.setTaxAmount(BigDecimal.ZERO);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());

            // 获取产品信息
            Product product = productRepository.selectById(itemRequest.getProductId());
            if (product != null) {
                orderItem.setSku(product.getSku());
                orderItem.setProductName(product.getName());
            }

            // 计算小计
            orderItem.calculateTotalPrice();

            orderItemRepository.insert(orderItem);
        }
    }

    public void calculateOrderTotal(Long orderId) {
        // 查询订单项
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<OrderItem> items = orderItemRepository.selectList(wrapper);

        //todo 这里应该根据客服分层价格来计算



        // 计算商品总额
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 更新订单
        Order order = getOrderEntityById(orderId);
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount
                .subtract(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                .add(order.getShippingAmount() != null ? order.getShippingAmount() : BigDecimal.ZERO)
                .add(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO));
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(getCurrentUserId());

        orderRepository.updateById(order);
    }

    private OrderResponse enrichOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();

        // 复制基本字段
        response.setId(order.getId());
        response.setPlatformOrderNo(order.getPlatformOrderId());
        response.setCustomerId(order.getCustomerId());
        response.setCurrency(order.getCurrency());
        response.setOrderType(order.getOrderType());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setOrderStatus(order.getOrderStatus());

        //销售id
        response.setSalesUserId(order.getSalesId());
        User saleUser = userRepository.selectById(order.getSalesId());
        response.setSalesUserName(saleUser.getUsername());
        //供应商id
        response.setSupplierCompanyId(order.getSupplierCompanyId());
        Company company = companyRepository.selectById(order.getSupplierCompanyId());
        response.setSupplierCompanyName(company.getName());

        //订单状态
        response.setStatus(order.getOrderStatus());
        response.setSource(order.getOrderSource());
        ;
        response.setAiProcessed(order.getAiProcessed());
        response.setAiConfidence(order.getAiConfidence()!=null?order.getAiConfidence().toString():BigDecimal.ZERO.toString());
        response.setProvinceName(order.getProvinceName());
        response.setCityName(order.getCityName());
        response.setDistrictName(order.getDistrictName());
        response.setTotalAmount(order.getFinalAmount());

        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setAddress(order.getDeliveryAddress());
        response.setDeliveryContact(order.getDeliveryContact());
        response.setDeliveryPhone(order.getDeliveryPhone());
        response.setExpectedDeliveryDate(order.getExpectedDeliveryDate());

        // 获取客户信息
        if (order.getCustomerId() != null && order.getCustomerId().longValue()!=0L) {
            Customer customer = customerRepository.selectById(order.getCustomerId());
            if (customer != null) {
                response.setCustomerName(customer.getName());
            }
        }



        // 获取订单项
        QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", order.getId());
        List<OrderItem> items = orderItemRepository.selectList(itemWrapper);
        
        List<OrderResponse.OrderItemResponse> itemResponses = items.stream()
                .map(this::convertToOrderItemResponse)
                .toList();
        response.setOrderItems(itemResponses);

        return response;
    }

    private OrderResponse.OrderItemResponse convertToOrderItemResponse(OrderItem item) {
        OrderResponse.OrderItemResponse response = new OrderResponse.OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductSku(item.getSku());
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getSubtotal());

        return response;
    }

    private Long getCurrentUserId() {
        // TODO: 从Spring Security上下文获取当前用户ID
        UserDetails userDetails=((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        String userName = userDetails.getUsername();

        User user = userRepository.findByUsername(userName);


        return user.getId();
    }


}
