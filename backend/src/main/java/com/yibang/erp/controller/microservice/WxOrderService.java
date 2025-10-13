package com.yibang.erp.controller.microservice;

import com.yibang.erp.domain.dto.OrderStatusUpdateRequest;
import com.yibang.erp.domain.entity.Order;

public interface WxOrderService {

    public void notifyWxOrderDeliveryPrepare(OrderStatusUpdateRequest request, Order order);
}
