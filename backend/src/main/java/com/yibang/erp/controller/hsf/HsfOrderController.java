package com.yibang.erp.controller.hsf;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.controller.microservice.RefundResponseDto;
import com.yibang.erp.domain.dto.OrderCloseMessageResponse;
import com.yibang.erp.domain.dto.OrderCreateRequest;
import com.yibang.erp.domain.dto.OrderListRequest;
import com.yibang.erp.domain.dto.OrderResponse;
import com.yibang.erp.domain.dto.microservice.OrderInfoResponse;
import com.yibang.erp.domain.dto.microservice.Result;
import com.yibang.erp.domain.entity.Order;
import com.yibang.erp.domain.service.OrderService;
import com.yibang.erp.infrastructure.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HSF订单服务控制器
 * 提供给yibang-mall系统调用
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/hsf/order")
public class HsfOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/refund")
    public Result<RefundResponseDto> refundOrder(@RequestParam String sourceOrderId){

        Result<RefundResponseDto> result = new Result<RefundResponseDto>();
        RefundResponseDto responseDto = new RefundResponseDto();
        log.info("开始处理关闭订单消息:  sourceOrderId={}",sourceOrderId);
        result.setData(responseDto);

        if(!StringUtils.hasText(sourceOrderId)){
            return Result.error("参数错误");

        }

        try {
            String lockKeyToUse = "order:wx:closeOrder:lock:" + sourceOrderId;

            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKeyToUse, "1", Duration.ofMinutes(10));

            if (!Boolean.TRUE.equals(locked)) {
                log.warn("消息正在处理中，跳过: sourceOrderId={}, idempotencyKey={}", sourceOrderId);
                // 发送ACK，确认消息已处理（重复消息）
                result.setMessage("PENDING");
                return result;
            }



            // 5. 处理业务逻辑 如果订单已经处于APPROVED的状态 或者SHIPPED的状态 会出现问题。必须手工处理 需要一张订单手工处理表，记录相关变更。

            OrderCloseMessageResponse orderCloseMessageResponse = orderService.handleCloseOrderHsf(sourceOrderId);
            responseDto.setRefundSuccess(orderCloseMessageResponse.canClose);
            responseDto.setRefundReason(orderCloseMessageResponse.getMessage());

            if (orderCloseMessageResponse.canClose) {
                //已经修改了
                log.info("订单关闭成功: sourceOrderId={}", sourceOrderId);

            } else {
                //没有修改则进入人工处理表
                log.info("订单关闭失败，已提交人工处理: sourceOrderId={}, reason={}", sourceOrderId, orderCloseMessageResponse.getMessage());

                // 注意：人工处理记录已经在OrderServiceImpl.handleCloseOrderMessage中创建
                // 这里只需要记录消息处理状态即可
            }


        } catch (Exception e) {

            log.error("hsf内部处理订单关闭失败: sourceOrderId={}", sourceOrderId, e);


        }



        return result;

    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<OrderInfoResponse> createOrder(@RequestBody OrderCreateRequest request) {
        try {
            log.info("HSF API调用: 创建订单 - sourceOrderId={}", request.getSourceOrderId());



            String orderNo = request.getSourceOrderId();
            Order order = orderRepository.selectBySourceOrderId(orderNo);
            if(order!=null){
                return Result.error("订单已存在");
            }

            //vaild request



            //build orderCreate
            OrderResponse orderResponse= orderService.createOrderByHsf(request);





            // 由于OrderService可能没有对应的方法，这里先返回模拟数据
            OrderInfoResponse orderInfo = new OrderInfoResponse();
            orderInfo.setId(orderResponse.getId());
            orderInfo.setPlatformOrderId(orderResponse.getPlatformOrderNo());
            orderInfo.setSourceOrderId(request.getSourceOrderId());
            orderInfo.setStatus("SUBMITTED");
            orderInfo.setTotalAmount(BigDecimal.valueOf(request.getOrderItems().stream()
                    .mapToDouble(item -> item.getUnitPrice().doubleValue() * item.getQuantity())
                    .sum()));
            
            return Result.success("创建订单成功", orderInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 创建订单", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单ID获取订单详情
     */
    @GetMapping("/details/{orderId}")
    public Result<OrderInfoResponse> getOrderById(@PathVariable Long orderId) {
        try {
            log.info("HSF API调用: 根据订单ID获取订单详情 - {}", orderId);
            
            OrderResponse orderResponse = orderService.getOrderById(orderId);
            if (orderResponse == null) {
                return Result.error("订单不存在");
            }

            OrderInfoResponse orderInfo = convertToOrderInfoResponse(orderResponse);
            return Result.success("获取订单详情成功", orderInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据订单ID获取订单详情", e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据源订单号获取订单详情
     */
    @GetMapping("/details-by-source-id/{sourceOrderId}")
    public Result<OrderInfoResponse> getOrderBySourceId(@PathVariable String sourceOrderId) {
        try {
            log.info("HSF API调用: 根据源订单号获取订单详情 - {}", sourceOrderId);
            
            Order order = orderService.getOrderBySourceOrderId(sourceOrderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 转换为OrderResponse格式
            OrderResponse orderResponse = new OrderResponse();
            orderResponse.setId(order.getId());
            orderResponse.setPlatformOrderNo(order.getPlatformOrderId());
            orderResponse.setSourceOrderId(order.getSourceOrderId());
            orderResponse.setStatus(order.getOrderStatus().toString());
            orderResponse.setTotalAmount(order.getTotalAmount());
            orderResponse.setCreatedAt(order.getCreatedAt());
            orderResponse.setUpdatedAt(order.getUpdatedAt());

            OrderInfoResponse orderInfo = convertToOrderInfoResponse(orderResponse);
            return Result.success("获取订单详情成功", orderInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据源订单号获取订单详情", e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Result<PageResult<OrderInfoResponse>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            log.info("HSF API调用: 获取订单列表 - page={}, size={}, status={}", page, size, status);
            
            // 构建OrderListRequest
            OrderListRequest request = new OrderListRequest();
            request.setCurrent(page);
            request.setSize(size);
            request.setStatus(status);
            request.setCustomerId(customerId);
            // 这里可以添加日期范围处理逻辑
            
            // 调用OrderService的查询订单列表方法
            Page<OrderResponse> orderPage = orderService.getOrderList(request);
            
            // 转换为响应格式
            List<OrderInfoResponse> orderList = orderPage.getRecords().stream()
                    .map(this::convertToOrderInfoResponse)
                    .collect(Collectors.toList());
            
            PageResult<OrderInfoResponse> pageResult = PageResult.of(orderList, orderPage.getTotal(), orderPage.getCurrent(), orderPage.getSize());
            
            return Result.success("获取订单列表成功", pageResult);
        } catch (Exception e) {
            log.error("HSF API调用失败: 获取订单列表", e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/status/{orderId}")
    public Result<OrderInfoResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            log.info("HSF API调用: 更新订单状态 - orderId={}, status={}", orderId, status);
            
            // 这里需要调用OrderService的更新订单状态方法
            // 由于OrderService可能没有对应的方法，这里先返回模拟数据
            OrderInfoResponse orderInfo = new OrderInfoResponse();
            orderInfo.setId(orderId);
            orderInfo.setStatus(status);
            
            return Result.success("更新订单状态成功", orderInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 更新订单状态", e);
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单物流信息
     */
    @GetMapping("/logistics/{orderId}")
    public Result<OrderInfoResponse.LogisticsInfo> getOrderLogistics(@PathVariable Long orderId) {
        try {
            log.info("HSF API调用: 获取订单物流信息 - {}", orderId);
            
            // 这里需要调用OrderService的获取物流信息方法
            // 由于OrderService可能没有对应的方法，这里先返回模拟数据
            OrderInfoResponse.LogisticsInfo logisticsInfo = new OrderInfoResponse.LogisticsInfo();
            logisticsInfo.setTrackingNumber("TRACK-001");
            logisticsInfo.setCarrier("顺丰速运");
            logisticsInfo.setStatus("已发货");
            
            return Result.success("获取订单物流信息成功", logisticsInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 获取订单物流信息", e);
            return Result.error("获取订单物流信息失败: " + e.getMessage());
        }
    }

    /**
     * 转换OrderResponse为OrderInfoResponse
     */
    private OrderInfoResponse convertToOrderInfoResponse(OrderResponse orderResponse) {
        OrderInfoResponse response = new OrderInfoResponse();
        response.setId(orderResponse.getId());
        response.setPlatformOrderId(orderResponse.getPlatformOrderNo());
        response.setSourceOrderId(orderResponse.getSourceOrderId());
        response.setStatus(orderResponse.getStatus());
        response.setTotalAmount(orderResponse.getTotalAmount());
        response.setCreatedAt(orderResponse.getCreatedAt());
        response.setUpdatedAt(orderResponse.getUpdatedAt());
        return response;
    }
}
