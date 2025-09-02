package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yibang.erp.domain.dto.ErrorOrderInfo;
import com.yibang.erp.domain.entity.AIExcelErrorOrder;
import com.yibang.erp.domain.service.AIExcelErrorOrderService;
import com.yibang.erp.infrastructure.repository.AIExcelErrorOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Excel错误订单服务实现类
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AIExcelErrorOrderServiceImpl extends ServiceImpl<AIExcelErrorOrderRepository, AIExcelErrorOrder> 
    implements AIExcelErrorOrderService {

    @Autowired
    private AIExcelErrorOrderRepository errorOrderRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void saveErrorOrder(String taskId, int rowNumber, Map<String, Object> rawData, 
                             String errorType, String errorMessage, String suggestedAction) {
        try {
            AIExcelErrorOrder errorOrder = new AIExcelErrorOrder();
            errorOrder.setTaskId(taskId);
            errorOrder.setExcelRowNumber(rowNumber);
            errorOrder.setRawData(convertMapToJson(rawData));
            errorOrder.setErrorType(errorType);
            errorOrder.setErrorMessage(errorMessage);
            errorOrder.setSuggestedAction(suggestedAction);
            errorOrder.setErrorLevel("ERROR");
            errorOrder.setStatus("PENDING");
            errorOrder.setCreatedAt(LocalDateTime.now());
            errorOrder.setUpdatedAt(LocalDateTime.now());

            errorOrderRepository.insert(errorOrder);
            log.info("保存错误订单成功: taskId={}, rowNumber={}, errorType={}", 
                    taskId, rowNumber, errorType);

        } catch (Exception e) {
            log.error("保存错误订单失败: taskId={}, rowNumber={}", taskId, rowNumber, e);
        }
    }

    @Override
    public void saveErrorOrders(List<Map<String, Object>> errorOrders) {
        if (errorOrders == null || errorOrders.isEmpty()) {
            return;
        }

        List<AIExcelErrorOrder> entities = errorOrders.stream()
                .map(this::convertToEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!entities.isEmpty()) {
            this.saveBatch(entities);
            log.info("批量保存错误订单成功: 数量={}", entities.size());
        }
    }

    @Override
    public List<ErrorOrderInfo> getErrorOrdersByTaskId(String taskId) {
        try {
            // 使用MyBatis-Plus的QueryWrapper来查询
            QueryWrapper<AIExcelErrorOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task_id", taskId);
            queryWrapper.orderByAsc("excel_row_number");
            
            List<AIExcelErrorOrder> entities = errorOrderRepository.selectList(queryWrapper);
            log.info("查询到错误订单实体数量: {}", entities.size());
            
            return entities.stream()
                    .map(this::convertToDto)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询错误订单失败，任务ID: {}", taskId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public IPage<ErrorOrderInfo> getUserErrorOrders(Long userId, Long companyId, int page, int size) {
        Page<AIExcelErrorOrder> pageParam = new Page<>(page, size);
        IPage<AIExcelErrorOrder> entityPage = errorOrderRepository.selectByUserAndCompany(pageParam, userId, companyId);
        
        // 转换为DTO
        IPage<ErrorOrderInfo> dtoPage = new Page<>(page, size, entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
        
        return dtoPage;
    }

    @Override
    public List<ErrorOrderInfo> getErrorOrdersByType(String errorType) {
        List<AIExcelErrorOrder> entities = errorOrderRepository.selectByErrorType(errorType);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ErrorOrderInfo> getErrorOrdersByStatus(String status) {
        List<AIExcelErrorOrder> entities = errorOrderRepository.selectByStatus(status);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void markErrorOrderAsProcessed(Long errorOrderId, Long operatorId) {
        AIExcelErrorOrder errorOrder = errorOrderRepository.selectById(errorOrderId);
        if (errorOrder != null) {
            errorOrder.setStatus("PROCESSED");
            errorOrder.setProcessedBy(operatorId);
            errorOrder.setProcessedAt(LocalDateTime.now());
            errorOrder.setUpdatedAt(LocalDateTime.now());
            errorOrderRepository.updateById(errorOrder);
            log.info("标记错误订单为已处理: errorOrderId={}, operatorId={}", errorOrderId, operatorId);
        }
    }

    @Override
    public void markErrorOrderAsIgnored(Long errorOrderId, Long operatorId) {
        AIExcelErrorOrder errorOrder = errorOrderRepository.selectById(errorOrderId);
        if (errorOrder != null) {
            errorOrder.setStatus("IGNORED");
            errorOrder.setProcessedBy(operatorId);
            errorOrder.setProcessedAt(LocalDateTime.now());
            errorOrder.setUpdatedAt(LocalDateTime.now());
            errorOrderRepository.updateById(errorOrder);
            log.info("标记错误订单为已忽略: errorOrderId={}, operatorId={}", errorOrderId, operatorId);
        }
    }

    @Override
    public Map<String, Object> getErrorOrderStatistics(String taskId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总错误数量
        Long totalErrors = errorOrderRepository.countByTaskId(taskId);
        statistics.put("totalErrors", totalErrors);
        
        // 按错误类型统计
        List<AIExcelErrorOrder> allErrors = errorOrderRepository.selectByTaskId(taskId);
        Map<String, Long> errorTypeCount = allErrors.stream()
                .collect(Collectors.groupingBy(AIExcelErrorOrder::getErrorType, Collectors.counting()));
        statistics.put("errorTypeCount", errorTypeCount);
        
        // 按状态统计
        Map<String, Long> statusCount = allErrors.stream()
                .collect(Collectors.groupingBy(AIExcelErrorOrder::getStatus, Collectors.counting()));
        statistics.put("statusCount", statusCount);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getUserErrorOrderStatistics(Long userId, Long companyId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总错误数量
        Long totalErrors = errorOrderRepository.countByUser(userId, companyId);
        statistics.put("totalErrors", totalErrors);
        
        // 待处理数量
        List<AIExcelErrorOrder> pendingErrors = errorOrderRepository.selectByStatus("PENDING");
        statistics.put("pendingCount", (long) pendingErrors.size());
        
        // 已处理数量
        List<AIExcelErrorOrder> processedErrors = errorOrderRepository.selectByStatus("PROCESSED");
        statistics.put("processedCount", (long) processedErrors.size());
        
        return statistics;
    }

    /**
     * 将Map转换为JSON字符串
     */
    private String convertMapToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("转换Map为JSON失败", e);
            return "{}";
        }
    }

    /**
     * 将Map转换为实体对象
     */
    private AIExcelErrorOrder convertToEntity(Map<String, Object> errorData) {
        try {
            AIExcelErrorOrder entity = new AIExcelErrorOrder();
            entity.setTaskId((String) errorData.get("taskId"));
            entity.setExcelRowNumber((Integer) errorData.get("excelRowNumber"));
            entity.setRawData(convertMapToJson((Map<String, Object>) errorData.get("rawData")));
            entity.setErrorType((String) errorData.get("errorType"));
            entity.setErrorMessage((String) errorData.get("errorMessage"));
            entity.setSuggestedAction((String) errorData.get("suggestedAction"));
            entity.setErrorLevel("ERROR");
            entity.setStatus("PENDING");
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            return entity;
        } catch (Exception e) {
            log.error("转换Map为实体对象失败", e);
            return null;
        }
    }

    /**
     * 将实体对象转换为DTO
     */
    private ErrorOrderInfo convertToDto(AIExcelErrorOrder entity) {
        try {
            ErrorOrderInfo dto = new ErrorOrderInfo();
            dto.setId(entity.getId());
            dto.setTaskId(entity.getTaskId());
            dto.setExcelRowNumber(entity.getExcelRowNumber());
            dto.setRawData(convertJsonToMap(entity.getRawData()));
            dto.setErrorType(entity.getErrorType());
            dto.setErrorMessage(entity.getErrorMessage());
            dto.setErrorLevel(entity.getErrorLevel());
            dto.setSuggestedAction(entity.getSuggestedAction());
            dto.setStatus(entity.getStatus());
            dto.setProcessedBy(entity.getProcessedBy());
            dto.setProcessedAt(entity.getProcessedAt());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedAt(entity.getUpdatedAt());
            return dto;
        } catch (Exception e) {
            log.error("转换实体对象为DTO失败: entityId={}", entity.getId(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为Map
     */
    private Map<String, Object> convertJsonToMap(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("转换JSON为Map失败: json={}", json, e);
            return new HashMap<>();
        }
    }
}
