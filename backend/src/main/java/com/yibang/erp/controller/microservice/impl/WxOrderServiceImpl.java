package com.yibang.erp.controller.microservice.impl;

import com.yibang.erp.controller.microservice.OrderDeliveryRequest;
import com.yibang.erp.controller.microservice.WxOrderDeliveryClient;
import com.yibang.erp.controller.microservice.WxOrderService;
import com.yibang.erp.domain.dto.OrderStatusUpdateRequest;
import com.yibang.erp.domain.dto.microservice.Result;
import com.yibang.erp.domain.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WxOrderServiceImpl implements WxOrderService {


    @Autowired
    private WxOrderDeliveryClient wxOrderDeliveryClient;

    @Value("${hsf.api.key}")
    private String hsfApiKey;


    @Override
    public void notifyWxOrderDeliveryPrepare(OrderStatusUpdateRequest request, Order order) {
        if (request.getTargetStatus().equals("APPROVED") && order.getOrderSource().equals("WEBSITE") &&
                order.getSourceOrderId().startsWith("WX")) {
            OrderDeliveryRequest orderDeliveryRequest = new OrderDeliveryRequest();

            orderDeliveryRequest.setSourceOrderId(order.getSourceOrderId());
            orderDeliveryRequest.setOrderApproved(true);

            Result<String>  result= wxOrderDeliveryClient.orderDelivery(orderDeliveryRequest,hsfApiKey);
            if (result.isSuccess()) {
                log.info("微信订单发货准备成功，订单号：{}", order.getPlatformOrderId());
            } else {
                log.error("微信订单发货准备失败，订单号：{}", order.getPlatformOrderId());
            }
        }
    }
}
