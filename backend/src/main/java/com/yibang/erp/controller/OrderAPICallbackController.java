package com.yibang.erp.controller;

import com.yibang.erp.domain.entity.LogisticsInfo;
import com.yibang.erp.domain.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 订单API消息消费者 - 处理RabbitMQ消息
 */
@Slf4j
@Service
public class OrderAPICallbackController {

    @Value("${zsd.callback_prod.url}")
    private String callbackUrlProd;

    @Value("${zsd.callback_test.url}")
    private String callbackUrlTest;

    // shopId 固定值
    private static final String FIXED_SHOP_ID = "SHOP-001";

    private static final DateTimeFormatter SHIPPED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    private final RestTemplate restTemplate;

    public OrderAPICallbackController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 主动通知对方发货回调
     * @param order 订单
     * @param logisticsInfo 物流信息
     * @param useProd 是否使用线上地址
     * @return 是否成功
     */
    public boolean sendShipmentCallback(Order order, LogisticsInfo logisticsInfo, boolean useProd) {
        try {
            String url = useProd ? callbackUrlProd : callbackUrlTest;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer zxdx");
            headers.set("X-Correlation-Id", generateCorrelationId(order));
            headers.set("Idempotency-Key", generateIdempotencyKey(order));

            Map<String, Object> body = new HashMap<>();
            body.put("shopId", FIXED_SHOP_ID);
            body.put("orderId", order.getSourceOrderId());
            body.put("shippedAt", resolveShippedAt(logisticsInfo));
            body.put("trackingNumber", logisticsInfo != null ? logisticsInfo.getTrackingNumber() : order.getLogisticsOrderNumber());
            body.put("carrier", order.getLogisticsCompany());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("发送发货回调: url={}, body={}", url, body);
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, entity, (Class<Map<String, Object>>)(Class<?>)Map.class);
            Map<String, Object> resp = response.getBody();

            boolean success = response.getStatusCode().is2xxSuccessful()
                    && resp != null
                    && Boolean.TRUE.equals(resp.get("rel"));

            if (!success) {
                log.warn("发货回调失败: status={}, resp={}", response.getStatusCode(), resp);
                return false;
            }

            log.info("发货回调成功: orderId={}, sourceOrderId={}", order.getId(), order.getSourceOrderId());
            return true;
        } catch (Exception e) {
            log.error("发货回调异常: orderId={}, sourceOrderId={}, err={}", order.getId(), order.getSourceOrderId(), e.getMessage(), e);
            return false;
        }
    }

    private String resolveShippedAt(LogisticsInfo logisticsInfo) {
        LocalDateTime time = (logisticsInfo != null && logisticsInfo.getShippingDate() != null)
                ? logisticsInfo.getShippingDate()
                : LocalDateTime.now();
        return time.format(SHIPPED_AT_FORMATTER);
    }

    private String generateCorrelationId(Order order) {
        return "platform-ship-callback-" + (order.getSourceOrderId() != null ? order.getSourceOrderId() : order.getPlatformOrderId()) + "-" + UUID.randomUUID();
    }

    private String generateIdempotencyKey(Order order) {
        String base = order.getSourceOrderId() != null ? order.getSourceOrderId() : order.getPlatformOrderId();
        return "ship-callback-" + base + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 主动通知对方：物流信息更新（批量轨迹）
     * Header 同发货回调；Body 如：
     * {
     *   "shopId":"SHOP-001",
     *   "orderId":"ORDER-20240901-001",
     *   "logisticsInfo":[{"trackingNumber":"SF123","carrier":"顺丰速运"}]
     * }
     */
    public boolean sendLogisticsUpdate(String shopId,
                                       String sourceOrderId,
                                       java.util.List<Map<String, String>> logisticsInfoList,
                                       boolean useProd) {
        try {
            String url = useProd ? callbackUrlProd : callbackUrlTest;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer zxdx");
            headers.set("X-Correlation-Id", "platform-logistics-update-" + sourceOrderId + "-" + UUID.randomUUID());
            headers.set("Idempotency-Key", "logistics-update-" + sourceOrderId + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            Map<String, Object> body = new HashMap<>();
            body.put("shopId", shopId != null ? shopId : FIXED_SHOP_ID);
            body.put("orderId", sourceOrderId);
            body.put("logisticsInfo", logisticsInfoList);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            log.info("发送物流更新回调: url={}, body={}", url, body);

            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, entity, (Class<Map<String, Object>>)(Class<?>)Map.class);
            Map<String, Object> resp = response.getBody();

            boolean success = response.getStatusCode().is2xxSuccessful()
                    && resp != null
                    && Boolean.TRUE.equals(resp.get("success"));
            if (!success) {
                log.warn("物流更新回调失败: status={}, resp={}", response.getStatusCode(), resp);
                return false;
            }

            log.info("物流更新回调成功: sourceOrderId={}", sourceOrderId);
            return true;
        } catch (Exception e) {
            log.error("物流更新回调异常: sourceOrderId={}, err={}", sourceOrderId, e.getMessage(), e);
            return false;
        }
    }

}
