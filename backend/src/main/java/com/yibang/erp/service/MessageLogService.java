package com.yibang.erp.service;

import cn.hutool.json.JSONObject;
import com.yibang.erp.domain.dto.AddressChangeMessage;
import com.yibang.erp.domain.dto.OrderMessage;
import com.yibang.erp.domain.dto.OrderStatusChangeMessage;
import com.yibang.erp.domain.entity.MessageProcessingLog;
import com.yibang.erp.infrastructure.repository.MessageProcessingLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 消息日志服务
 * 用于异步记录消息处理状态，确保不受业务事务回滚影响
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
public class MessageLogService {

    @Autowired
    private MessageProcessingLogRepository messageLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordOrderStatusChangeMessageProcessing(String messageId, OrderStatusChangeMessage orderStatusChangeMessage , String busType, String status, String resultMessage) {
        try {

            MessageProcessingLog messageProcessingLog = new MessageProcessingLog();
            messageProcessingLog.setMessageId(messageId);
            messageProcessingLog.setSourceOrderId(orderStatusChangeMessage.getOrderId());
            messageProcessingLog.setSalesOrderId(orderStatusChangeMessage.getOrderId());
            messageProcessingLog.setIdempotencyKey(orderStatusChangeMessage.getIdempotencyKey());
            messageProcessingLog.setBusType(busType);
            messageProcessingLog.setTotalMessage(new JSONObject(orderStatusChangeMessage).toString());
            messageProcessingLog.setStatus(status);
            messageProcessingLog.setResultMessage(resultMessage);
            messageProcessingLog.setCreatedAt(LocalDateTime.now());
            messageProcessingLog.setUpdatedAt(LocalDateTime.now());

            if ("SUCCESS".equals(status) || "DUPLICATE".equals(status)) {
                messageProcessingLog.setProcessedAt(LocalDateTime.now());
            }

            messageLogRepository.insert(messageProcessingLog);

            log.info("消息处理状态已记录: messageId={}, status={}", messageId, status);

        } catch (Exception e) {
            // 如果是重复键错误，尝试更新现有记录
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                try {
                    // 查找现有记录
                    MessageProcessingLog existingLog = messageLogRepository.selectByIdempotencyKey(orderStatusChangeMessage.getIdempotencyKey());
                    if (existingLog != null) {
                        // 更新现有记录
                        existingLog.setStatus(status);
                        existingLog.setResultMessage(resultMessage);
                        existingLog.setUpdatedAt(LocalDateTime.now());
                        if ("SUCCESS".equals(status) || "DUPLICATE".equals(status)) {
                            existingLog.setProcessedAt(LocalDateTime.now());
                        }
                        messageLogRepository.updateById(existingLog);
                        log.info("消息处理状态已更新: messageId={}, status={}", messageId, status);
                    }
                } catch (Exception updateException) {
                    log.error("更新消息处理状态失败: messageId={}", messageId, updateException);
                }
            } else {
                log.error("记录消息处理状态失败: messageId={}", messageId, e);
            }
        }
    }



    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAddressChangeMessageProcessing(String messageId, AddressChangeMessage message, String busType, String status, String resultMessage) {
        try {

            MessageProcessingLog messageProcessingLog = new MessageProcessingLog();
            messageProcessingLog.setMessageId(messageId);
            messageProcessingLog.setSourceOrderId(message.getOrderId());
            messageProcessingLog.setSalesOrderId(message.getOrderId());
            messageProcessingLog.setIdempotencyKey(message.getIdempotencyKey());
            messageProcessingLog.setBusType(busType);
            messageProcessingLog.setStatus(status);
            messageProcessingLog.setTotalMessage(new JSONObject(message).toString());
            messageProcessingLog.setResultMessage(resultMessage);
            messageProcessingLog.setCreatedAt(LocalDateTime.now());
            messageProcessingLog.setUpdatedAt(LocalDateTime.now());

            if ("SUCCESS".equals(status) || "DUPLICATE".equals(status)) {
                messageProcessingLog.setProcessedAt(LocalDateTime.now());
            }

            messageLogRepository.insert(messageProcessingLog);

            log.info("消息处理状态已记录: messageId={}, status={}", messageId, status);

        } catch (Exception e) {
            // 如果是重复键错误，尝试更新现有记录
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                try {
                    // 查找现有记录
                    MessageProcessingLog existingLog = messageLogRepository.selectByIdempotencyKey(message.getIdempotencyKey());
                    if (existingLog != null) {
                        // 更新现有记录
                        existingLog.setStatus(status);
                        existingLog.setResultMessage(resultMessage);
                        existingLog.setUpdatedAt(LocalDateTime.now());
                        if ("SUCCESS".equals(status) || "DUPLICATE".equals(status)) {
                            existingLog.setProcessedAt(LocalDateTime.now());
                        }
                        messageLogRepository.updateById(existingLog);
                        log.info("消息处理状态已更新: messageId={}, status={}", messageId, status);
                    }
                } catch (Exception updateException) {
                    log.error("更新消息处理状态失败: messageId={}", messageId, updateException);
                }
            } else {
                log.error("记录消息处理状态失败: messageId={}", messageId, e);
            }
        }
    }


    /**
     * 异步记录消息处理日志
     * 使用独立的事务，确保不受业务事务回滚影响
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordMessageProcessing(String messageId, OrderMessage message, String busType, String status, String resultMessage) {
        try {






            MessageProcessingLog messageProcessingLog = new MessageProcessingLog();
            messageProcessingLog.setMessageId(messageId);
            messageProcessingLog.setSourceOrderId(message.getOrderId());
            messageProcessingLog.setSalesOrderId(message.getOrderId());
            messageProcessingLog.setIdempotencyKey(message.getIdempotencyKey());
            messageProcessingLog.setTotalMessage(new JSONObject(message).toString());
            messageProcessingLog.setBusType(busType);
            messageProcessingLog.setStatus(status);
            messageProcessingLog.setResultMessage(resultMessage);
            messageProcessingLog.setCreatedAt(LocalDateTime.now());
            messageProcessingLog.setUpdatedAt(LocalDateTime.now());
            
            if ("SUCCESS".equals(status) || "DUPLICATE".equals(status)) {
                messageProcessingLog.setProcessedAt(LocalDateTime.now());
            }
            
            messageLogRepository.insert(messageProcessingLog);

            log.info("消息处理状态已记录: messageId={}, status={}", messageId, status);

        } catch (Exception e) {
            // 如果是重复键错误，尝试更新现有记录
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                try {
                    // 查找现有记录
                    MessageProcessingLog existingLog = messageLogRepository.selectByIdempotencyKey(message.getIdempotencyKey());
                    if (existingLog != null) {
                        // 更新现有记录
                        existingLog.setStatus(status);
                        existingLog.setResultMessage(resultMessage);
                        existingLog.setUpdatedAt(LocalDateTime.now());
                        if ("SUCCESS".equals(status) || "DUPLICATE".equals(status)) {
                            existingLog.setProcessedAt(LocalDateTime.now());
                        }
                        messageLogRepository.updateById(existingLog);
                        log.info("消息处理状态已更新: messageId={}, status={}", messageId, status);
                    }
                } catch (Exception updateException) {
                    log.error("更新消息处理状态失败: messageId={}", messageId, updateException);
                }
            } else {
                log.error("记录消息处理状态失败: messageId={}", messageId, e);
            }
        }
    }
}
