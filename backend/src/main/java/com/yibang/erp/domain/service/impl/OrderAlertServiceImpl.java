package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.PageUtils;
import com.yibang.erp.domain.dto.ManualProcessingRequest;
import com.yibang.erp.domain.dto.OrderAlertRequest;
import com.yibang.erp.domain.dto.OrderAlertResponse;
import com.yibang.erp.domain.entity.Order;
import com.yibang.erp.domain.entity.OrderManualProcessing;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.OrderAlertService;
import com.yibang.erp.infrastructure.repository.OrderManualProcessingRepository;
import com.yibang.erp.infrastructure.repository.OrderRepository;
import com.yibang.erp.infrastructure.repository.UserRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单预警服务实现
 */
@Slf4j
@Service
public class OrderAlertServiceImpl implements OrderAlertService {

    @Autowired
    private OrderManualProcessingRepository orderManualProcessingRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRedisRepository userRedisRepository;

    @Override
    public PageResult<OrderAlertResponse> getOrderAlerts(OrderAlertRequest request, Long companyId, Long userId) {
        // 验证分页参数
        PageUtils.validatePageRequest(request);
        
        // 检查用户权限
        boolean isSystemAdmin = isSystemAdmin();
        boolean isSupplierAdmin = isSupplierAdmin();
        
        // 构建查询条件
        LambdaQueryWrapper<OrderManualProcessing> queryWrapper = new LambdaQueryWrapper<>();
        
        // 权限控制
        if (isSystemAdmin) {
            // 系统管理员可以看到所有预警
        } else if (isSupplierAdmin) {
            // 供应商管理员只能看到相关订单的预警
            // 这里需要通过订单关联查询，暂时简化处理

            //既然是相关的订单预警，那么就是自己公司
            queryWrapper.eq(OrderManualProcessing::getSupplierCompanyId, companyId);
        } else {
            // 销售用户只能看到自己创建的预警 销售用户能看到自己公司销售人员的订单预警
            User salesUserBase=userRedisRepository.selectById(userId);
            List<User> salesUsers =userRedisRepository.findByCompanyId(salesUserBase.getCompanyId());

            List<Long> salesUserIds =salesUsers.stream().map(User::getId).toList();

            queryWrapper.in(OrderManualProcessing::getCreatedBy, salesUserIds);
        }
        
        // 添加查询条件
        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq(OrderManualProcessing::getStatus, request.getStatus());
        }
        
        if (StringUtils.hasText(request.getProcessingType())) {
            queryWrapper.eq(OrderManualProcessing::getProcessingType, request.getProcessingType());
        }
        
        if (StringUtils.hasText(request.getSourceOrderId())) {
            queryWrapper.like(OrderManualProcessing::getSourceOrderId, request.getSourceOrderId());
        }
        
        // 时间范围查询
        if (StringUtils.hasText(request.getStartDate())) {
            LocalDateTime startTime = LocalDateTime.parse(request.getStartDate() + " 00:00:00", 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge(OrderManualProcessing::getCreatedAt, startTime);
        }
        
        if (StringUtils.hasText(request.getEndDate())) {
            LocalDateTime endTime = LocalDateTime.parse(request.getEndDate() + " 23:59:59", 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le(OrderManualProcessing::getCreatedAt, endTime);
        }
        
        // 排序
        if (StringUtils.hasText(request.getSortField())) {
            if ("priority".equals(request.getSortField())) {
                if ("asc".equals(request.getSortOrder())) {
                    queryWrapper.orderByAsc(OrderManualProcessing::getPriority);
                } else {
                    queryWrapper.orderByDesc(OrderManualProcessing::getPriority);
                }
            }
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(OrderManualProcessing::getCreatedAt);
        }
        
        // 获取总记录数
        long total = orderManualProcessingRepository.selectCount(queryWrapper);
        
        if (total == 0) {
            return PageUtils.createEmptyPageResult(request);
        }
        
        // 添加分页条件（手动实现分页）
        queryWrapper.last(String.format("LIMIT %d, %d", 
            request.getOffset(), request.getLimit()));
        
        // 查询数据
        List<OrderManualProcessing> alerts = orderManualProcessingRepository.selectList(queryWrapper);
        
        // 转换为响应对象
        List<OrderAlertResponse> responses = alerts.stream()
            .map(this::convertToOrderAlertResponse)
            .collect(Collectors.toList());
        
        // 创建分页结果
        return PageUtils.createPageResult(responses, total, request);
    }

    @Override
    public void assignAlert(Long alertId, Long userId, Long currentUserId) {
        OrderManualProcessing alert = orderManualProcessingRepository.selectById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("预警记录不存在");
        }
        
        // 检查权限
        if (!canModifyAlert(alert, currentUserId)) {
            throw new IllegalArgumentException("没有权限修改此预警");
        }
        
        alert.setAssignedTo(userId);
        alert.setUpdatedAt(LocalDateTime.now());
        alert.setUpdatedBy(currentUserId);
        
        orderManualProcessingRepository.updateById(alert);
        log.info("分配预警成功: alertId={}, userId={}", alertId, userId);
    }

    @Override
    public void processAlert(Long alertId, ManualProcessingRequest request, Long currentUserId) {
        OrderManualProcessing alert = orderManualProcessingRepository.selectById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("预警记录不存在");
        }
        
        // 检查权限 仅仅只有供应商有权限处理
        if (!canModifyAlert(alert, currentUserId)) {
            throw new IllegalArgumentException("没有权限修改此预警");
        }
        
        alert.setStatus(OrderManualProcessing.STATUS_PROCESSING);
        alert.setProcessedBy(currentUserId);
        alert.setProcessingNotes(request.getProcessingNotes());
        alert.setUpdatedAt(LocalDateTime.now());
        alert.setUpdatedBy(currentUserId);
        
        orderManualProcessingRepository.updateById(alert);
        log.info("开始处理预警成功: alertId={}, userId={}", alertId, currentUserId);
    }

    @Override
    public void completeAlert(Long alertId, ManualProcessingRequest request, Long currentUserId) {
        OrderManualProcessing alert = orderManualProcessingRepository.selectById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("预警记录不存在");
        }
        
        // 检查权限
        if (!canModifyAlert(alert, currentUserId)) {
            throw new IllegalArgumentException("没有权限修改此预警");
        }
        
        alert.setStatus(OrderManualProcessing.STATUS_COMPLETED);
        alert.setProcessedBy(currentUserId);
        alert.setProcessedAt(LocalDateTime.now());
        alert.setProcessingNotes(request.getProcessingNotes());
        alert.setUpdatedAt(LocalDateTime.now());
        alert.setUpdatedBy(currentUserId);
        
        orderManualProcessingRepository.updateById(alert);
        log.info("完成预警处理成功: alertId={}, userId={}", alertId, currentUserId);
    }

    @Override
    public void rejectAlert(Long alertId, ManualProcessingRequest request, Long currentUserId) {
        OrderManualProcessing alert = orderManualProcessingRepository.selectById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("预警记录不存在");
        }
        
        // 检查权限
        if (!canModifyAlert(alert, currentUserId)) {
            throw new IllegalArgumentException("没有权限修改此预警");
        }
        
        alert.setStatus(OrderManualProcessing.STATUS_REJECTED);
        alert.setProcessedBy(currentUserId);
        alert.setProcessedAt(LocalDateTime.now());
        alert.setRejectionReason(request.getRejectionReason());
        alert.setUpdatedAt(LocalDateTime.now());
        alert.setUpdatedBy(currentUserId);
        
        orderManualProcessingRepository.updateById(alert);
        log.info("拒绝预警处理成功: alertId={}, userId={}", alertId, currentUserId);
    }

    /**
     * 转换为OrderAlertResponse
     */
    private OrderAlertResponse convertToOrderAlertResponse(OrderManualProcessing alert) {
        OrderAlertResponse response = new OrderAlertResponse();
        response.setId(alert.getId());
        response.setOrderId(alert.getOrderId());
        response.setSourceOrderId(alert.getSourceOrderId());
        response.setProcessingType(alert.getProcessingType());
        response.setProcessingTypeDesc(getProcessingTypeDesc(alert.getProcessingType()));
        response.setProcessingReason(alert.getProcessingReason());
        response.setStatus(alert.getStatus());
        response.setStatusDesc(getStatusDesc(alert.getStatus()));
        response.setPriority(alert.getPriority());
        response.setPriorityDesc(getPriorityDesc(alert.getPriority()));
        response.setAssignedTo(alert.getAssignedTo());
        response.setProcessedBy(alert.getProcessedBy());
        response.setProcessedAt(alert.getProcessedAt());
        response.setProcessingNotes(alert.getProcessingNotes());
        response.setRejectionReason(alert.getRejectionReason());
        response.setCreatedAt(alert.getCreatedAt());
        response.setUpdatedAt(alert.getUpdatedAt());
        response.setCreatedBy(alert.getCreatedBy());
        
        // 设置用户名
        if (alert.getAssignedTo() != null) {
            User assignedUser = userRedisRepository.selectById(alert.getAssignedTo());
            if (assignedUser != null) {
                response.setAssignedToName(assignedUser.getUsername());
            }
        }
        
        if (alert.getProcessedBy() != null) {
            User processedUser = userRedisRepository.selectById(alert.getProcessedBy());
            if (processedUser != null) {
                response.setProcessedByName(processedUser.getUsername());
            }
        }
        
        if (alert.getCreatedBy() != null) {
            User createdUser = userRedisRepository.selectById(alert.getCreatedBy());
            if (createdUser != null) {
                response.setCreatedByName(createdUser.getUsername());
            }
        }
        
        // 设置订单信息
        if (alert.getOrderId() != null) {
            Order order = orderRepository.selectById(alert.getOrderId());
            if (order != null) {
                OrderAlertResponse.OrderBasicInfo orderInfo = new OrderAlertResponse.OrderBasicInfo();
                orderInfo.setOrderNumber(order.getPlatformOrderId());
                // 通过customerId获取客户名称
                if (order.getCustomerId() != null) {
                    // 这里可以通过customerId查询客户名称，暂时使用ID
                    orderInfo.setCustomerName("客户ID: " + order.getCustomerId());
                }
                orderInfo.setOrderStatus(order.getOrderStatus());
                orderInfo.setOrderStatusDesc(getOrderStatusDesc(order.getOrderStatus()));
                orderInfo.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0");
                orderInfo.setDeliveryAddress(order.getDeliveryAddress());
                orderInfo.setBuyerNote(order.getSpecialRequirements());
                response.setOrderInfo(orderInfo);
            }
        }
        
        return response;
    }

    /**
     * 检查是否可以修改预警
     */
    private boolean canModifyAlert(OrderManualProcessing alert, Long currentUserId) {
        boolean isSystemAdmin = isSystemAdmin();
        if (isSystemAdmin) {
            return true;
        }

        //和这个相关的供应链商家可以处理
        Long orderId = alert.getOrderId();

        Order order =orderRepository.selectById(orderId);

        if(order.getSupplierCompanyId().equals(userRedisRepository.selectById(currentUserId).getCompanyId())){
            return true;
        }

        
        // 检查是否是创建者或分配的处理人
        return alert.getCreatedBy().equals(currentUserId) || 
               (alert.getAssignedTo() != null && alert.getAssignedTo().equals(currentUserId));
    }

    /**
     * 检查是否是系统管理员
     */
    private boolean isSystemAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
    }

    /**
     * 检查是否是供应商管理员
     */
    private boolean isSupplierAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPPLIER_ADMIN"));
    }

    /**
     * 获取处理类型描述
     */
    private String getProcessingTypeDesc(String processingType) {
        Map<String, String> typeMap = Map.of(
            OrderManualProcessing.TYPE_ORDER_CLOSE, "订单关闭",
            OrderManualProcessing.TYPE_ADDRESS_CHANGE, "地址修改",
            OrderManualProcessing.TYPE_REFUND, "退款",
            OrderManualProcessing.TYPE_CANCEL, "取消订单"
        );
        return typeMap.getOrDefault(processingType, processingType);
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(String status) {
        Map<String, String> statusMap = Map.of(
            OrderManualProcessing.STATUS_PENDING, "待处理",
            OrderManualProcessing.STATUS_PROCESSING, "处理中",
            OrderManualProcessing.STATUS_COMPLETED, "已完成",
            OrderManualProcessing.STATUS_REJECTED, "已拒绝"
        );
        return statusMap.getOrDefault(status, status);
    }

    /**
     * 获取优先级描述
     */
    private String getPriorityDesc(String priority) {
        Map<String, String> priorityMap = Map.of(
            OrderManualProcessing.PRIORITY_LOW, "低",
            OrderManualProcessing.PRIORITY_NORMAL, "普通",
            OrderManualProcessing.PRIORITY_HIGH, "高",
            OrderManualProcessing.PRIORITY_URGENT, "紧急"
        );
        return priorityMap.getOrDefault(priority, priority);
    }

    /**
     * 获取订单状态描述
     */
    private String getOrderStatusDesc(String orderStatus) {
        Map<String, String> statusMap = Map.of(
            "PENDING", "待处理",
            "SUBMITTED", "已提交",
            "APPROVED", "已审核",
            "SHIPPED", "已发货",
            "DELIVERED", "已送达",
            "CANCELLED", "已取消",
            "RETURNED", "已退货"
        );
        return statusMap.getOrDefault(orderStatus, orderStatus);
    }
}
