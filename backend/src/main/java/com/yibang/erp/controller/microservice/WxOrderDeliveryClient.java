package com.yibang.erp.controller.microservice;

import com.yibang.erp.domain.dto.microservice.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "yibang-taskmall", path = "/api/hsf/order")
public interface WxOrderDeliveryClient {

    @PostMapping("/delivery")
    public Result<String> orderDelivery(@RequestBody OrderDeliveryRequest orderDeliveryRequest,@RequestHeader("X-API-Key") String apiKey) ;


}
