package com.yibang.erp.domain.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yibang.erp.domain.dto.OrderBatchProcessRequest;
import com.yibang.erp.domain.dto.OrderBatchProcessResponse;
import com.yibang.erp.domain.entity.ExcelImportLog;
import com.yibang.erp.domain.entity.Order;
import com.yibang.erp.domain.entity.OrderItem;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.infrastructure.repository.ExcelImportLogRepository;
import com.yibang.erp.infrastructure.repository.OrderItemRepository;
import com.yibang.erp.infrastructure.repository.OrderRepository;
import com.yibang.erp.domain.service.OrderBatchService;
import com.yibang.erp.domain.service.OrderNumberGeneratorService;
import com.yibang.erp.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单批量处理服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderBatchServiceImpl extends ServiceImpl<OrderRepository, Order> implements OrderBatchService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ExcelImportLogRepository excelImportLogRepository;

    @Autowired
    private OrderNumberGeneratorService orderNumberGeneratorService;

    @Autowired
    private UserRepository userRepository;

    // 批量处理进度缓存
    private final Map<String, BatchProgress> progressCache = new ConcurrentHashMap<>();

    @Override
    public OrderBatchProcessResponse importExcelOrders(MultipartFile file, Long templateId) {
        String batchId = generateBatchId();
        BatchProgress progress = new BatchProgress(batchId);
        progressCache.put(batchId, progress);

        try {
            // 创建导入日志
            ExcelImportLog importLog = createImportLog(file, batchId);
            excelImportLogRepository.insert(importLog);

            // 解析Excel文件
            List<OrderImportData> importDataList = parseExcelFile(file, progress);

            // 验证数据
            List<OrderImportData> validDataList = validateImportData(importDataList, progress);

            // 批量创建订单
            List<Order> createdOrders = batchCreateOrders(validDataList, templateId, progress);

            // 更新导入日志
            updateImportLog(importLog, progress, null);

            return buildBatchResponse(batchId, progress, createdOrders.size());

        } catch (Exception e) {
            // 更新导入日志（失败状态）
            ExcelImportLog importLog = excelImportLogRepository.selectByFileHash(generateFileHash(file));
            if (importLog != null) {
                updateImportLog(importLog, progress, e.getMessage());
            }

            progress.setStatus("FAILED");
            progress.setErrorMessage(e.getMessage());

            return buildBatchResponse(batchId, progress, 0);
        }
    }

    @Override
    public OrderBatchProcessResponse batchUpdateOrderStatus(OrderBatchProcessRequest request) {
        String batchId = generateBatchId();
        BatchProgress progress = new BatchProgress(batchId);
        progressCache.put(batchId, progress);

        try {
            List<Long> orderIds = request.getOrderIds();
            String newStatus = request.getNewStatus();
            String reason = request.getReason();

            // 验证订单状态
            validateOrderStatusTransition(orderIds, newStatus);

            // 批量更新状态
            UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", orderIds)
                    .set("order_status", newStatus)
                    .set("updated_at", LocalDateTime.now())
                    .set("updated_by", getCurrentUserId());

            int updatedCount = orderRepository.update(null, updateWrapper);

            // 记录状态变更日志
            recordStatusChangeLogs(orderIds, newStatus, reason);

            progress.setStatus("COMPLETED");
            progress.setProcessedRows(updatedCount);
            progress.setSuccessRows(updatedCount);

            return buildBatchResponse(batchId, progress, updatedCount);

        } catch (Exception e) {
            progress.setStatus("FAILED");
            progress.setErrorMessage(e.getMessage());
            return buildBatchResponse(batchId, progress, 0);
        }
    }

    @Override
    public OrderBatchProcessResponse batchDeleteOrders(OrderBatchProcessRequest request) {
        String batchId = generateBatchId();
        BatchProgress progress = new BatchProgress(batchId);
        progressCache.put(batchId, progress);

        try {
            List<Long> orderIds = request.getOrderIds();

            // 验证订单是否可以删除
            validateOrdersForDeletion(orderIds);

            // 批量删除订单项
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.in("order_id", orderIds);
            orderItemRepository.delete(itemWrapper);

            // 批量删除订单
            int deletedCount = orderRepository.deleteBatchIds(orderIds);

            progress.setStatus("COMPLETED");
            progress.setProcessedRows(deletedCount);
            progress.setSuccessRows(deletedCount);

            return buildBatchResponse(batchId, progress, deletedCount);

        } catch (Exception e) {
            progress.setStatus("FAILED");
            progress.setErrorMessage(e.getMessage());
            return buildBatchResponse(batchId, progress, 0);
        }
    }

    @Override
    public OrderBatchProcessResponse batchAssignSupplier(OrderBatchProcessRequest request) {
        String batchId = generateBatchId();
        BatchProgress progress = new BatchProgress(batchId);
        progressCache.put(batchId, progress);

        try {
            List<Long> orderIds = request.getOrderIds();
            Long supplierCompanyId = request.getSupplierCompanyId();

            // 验证供应商公司
            validateSupplierCompany(supplierCompanyId);

            // 批量分配供应商
            UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", orderIds)
                    .set("supplier_company_id", supplierCompanyId)
                    .set("updated_at", LocalDateTime.now())
                    .set("updated_by", getCurrentUserId());

            int updatedCount = orderRepository.update(null, updateWrapper);

            progress.setStatus("COMPLETED");
            progress.setProcessedRows(updatedCount);
            progress.setSuccessRows(updatedCount);

            return buildBatchResponse(batchId, progress, updatedCount);

        } catch (Exception e) {
            progress.setStatus("FAILED");
            progress.setErrorMessage(e.getMessage());
            return buildBatchResponse(batchId, progress, 0);
        }
    }

    @Override
    public OrderBatchProcessResponse getBatchProgress(String batchId) {
        BatchProgress progress = progressCache.get(batchId);
        if (progress == null) {
            throw new IllegalArgumentException("批量处理任务不存在: " + batchId);
        }
        return buildBatchResponse(batchId, progress, progress.getSuccessRows());
    }

    @Override
    public boolean cancelBatchProcess(String batchId) {
        BatchProgress progress = progressCache.get(batchId);
        if (progress == null) {
            return false;
        }

        if ("PROCESSING".equals(progress.getStatus())) {
            progress.setStatus("CANCELLED");
            return true;
        }

        return false;
    }

    /**
     * 解析Excel文件
     */
    private List<OrderImportData> parseExcelFile(MultipartFile file, BatchProgress progress) throws IOException {
        List<OrderImportData> dataList = new ArrayList<>();

        EasyExcel.read(file.getInputStream(), OrderImportData.class, new AnalysisEventListener<OrderImportData>() {
            @Override
            public void invoke(OrderImportData data, AnalysisContext context) {
                dataList.add(data);
                progress.incrementProcessedRows();
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                progress.setTotalRows(dataList.size());
            }
        }).sheet().doRead();

        return dataList;
    }

    /**
     * 验证导入数据
     */
    private List<OrderImportData> validateImportData(List<OrderImportData> dataList, BatchProgress progress) {
        List<OrderImportData> validDataList = new ArrayList<>();
        AtomicInteger errorCount = new AtomicInteger(0);

        for (OrderImportData data : dataList) {
            try {
                if (validateOrderData(data)) {
                    validDataList.add(data);
                    progress.incrementSuccessRows();
                } else {
                    progress.incrementFailedRows();
                    errorCount.incrementAndGet();
                }
            } catch (Exception e) {
                progress.incrementFailedRows();
                errorCount.incrementAndGet();
                progress.addError("行 " + (dataList.indexOf(data) + 1) + ": " + e.getMessage());
            }
        }

        return validDataList;
    }

    /**
     * 批量创建订单
     */
    private List<Order> batchCreateOrders(List<OrderImportData> dataList, Long templateId, BatchProgress progress) {
        List<Order> createdOrders = new ArrayList<>();

        try {
            // 预生成所有订单号，提高性能
//            Long accountId = getCurrentUserId();
            UserDetails userDetails = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            String userName = userDetails.getUsername();


            String orderSource = "EXCEL_IMPORT";
            List<String> orderNumbers = orderNumberGeneratorService.preGenerateOrderNumbers(userName, orderSource, dataList.size());

            for (int i = 0; i < dataList.size(); i++) {
                try {
                    Order order = buildOrderFromImportData(dataList.get(i), templateId);
                    // 使用预生成的订单号
                    order.setPlatformOrderId(orderNumbers.get(i));
                    orderRepository.insert(order);
                    createdOrders.add(order);
                    progress.incrementSuccessRows();
                } catch (Exception e) {
                    progress.incrementFailedRows();
                    progress.addError("创建订单失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            progress.setStatus("FAILED");
            progress.setErrorMessage("批量生成订单号失败: " + e.getMessage());
            log.error("批量生成订单号失败", e);
        }

        return createdOrders;
    }

    /**
     * 验证订单数据
     */
    private boolean validateOrderData(OrderImportData data) {
        // 基本验证
        if (!StringUtils.hasText(data.getCustomerName())) {
            return false;
        }

        if (data.getOrderItems() == null || data.getOrderItems().isEmpty()) {
            return false;
        }

        // 验证订单项
        for (OrderItemImportData item : data.getOrderItems()) {
            if (!StringUtils.hasText(item.getProductName()) || item.getQuantity() <= 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 从导入数据构建订单
     */
    private Order buildOrderFromImportData(OrderImportData data, Long templateId) {
        Order order = new Order();

        // 设置基本信息（订单号将在外部设置）
        order.setCustomerId(resolveCustomerId(data.getCustomerName()));
        order.setOrderStatus("DRAFT");
        order.setOrderSource("EXCEL_IMPORT");
        order.setOrderType("STANDARD");

        // 设置金额信息
        double totalAmount = data.getOrderItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        order.setTotalAmount(java.math.BigDecimal.valueOf(totalAmount));
        order.setCurrency("CNY");

        // 设置其他信息
        order.setDeliveryAddress(data.getDeliveryAddress());
        if (data.getExpectedDeliveryDate() != null && !data.getExpectedDeliveryDate().trim().isEmpty()) {
            try {
                order.setExpectedDeliveryDate(java.time.LocalDate.parse(data.getExpectedDeliveryDate()));
            } catch (Exception e) {
                // 如果日期格式不正确，设置为null
                order.setExpectedDeliveryDate(null);
            }
        }
        order.setRemarks(data.getRemarks());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setCreatedBy(getCurrentUserId());
        order.setUpdatedBy(getCurrentUserId());

        return order;
    }

    /**
     * 验证订单状态转换
     */
    private void validateOrderStatusTransition(List<Long> orderIds, String newStatus) {
        // 这里可以添加状态转换的业务规则验证
        // 例如：草稿状态不能直接变为已完成状态
        if ("DRAFT".equals(newStatus) && "COMPLETED".equals(newStatus)) {
            throw new IllegalStateException("草稿状态不能直接变为已完成状态");
        }
    }

    /**
     * 验证订单是否可以删除
     */
    private void validateOrdersForDeletion(List<Long> orderIds) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", orderIds)
                .in("order_status", Arrays.asList("CONFIRMED", "PROCESSING", "SHIPPED"));

        if (orderRepository.selectCount(queryWrapper) > 0) {
            throw new IllegalStateException("已确认、处理中或已发货的订单不能删除");
        }
    }

    /**
     * 验证供应商公司
     */
    private void validateSupplierCompany(Long supplierCompanyId) {
        // TODO: 验证供应商公司是否存在
        if (supplierCompanyId == null || supplierCompanyId <= 0) {
            throw new IllegalArgumentException("供应商公司ID无效");
        }
    }

    /**
     * 记录状态变更日志
     */
    private void recordStatusChangeLogs(List<Long> orderIds, String newStatus, String reason) {
        // TODO: 实现状态变更日志记录
        // 这里可以调用OrderStatusLogService来记录状态变更
    }

    /**
     * 创建导入日志
     */
    private ExcelImportLog createImportLog(MultipartFile file, String batchId) {
        ExcelImportLog log = new ExcelImportLog();
        log.setFileName(file.getOriginalFilename());
        log.setFileSize(file.getSize());
        log.setFileHash(generateFileHash(file));
        log.setImportStatus("PROCESSING");
        log.setImportUserId(getCurrentUserId());
        log.setStartedAt(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        return log;
    }

    /**
     * 生成文件哈希
     */
    private String generateFileHash(MultipartFile file) {
        try {
            // 使用MD5或SHA-256等哈希算法生成文件哈希
            // 这里使用MD5作为示例
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // 发生错误时返回空字符串或抛出异常
            return "";
        }
    }

    /**
     * 更新导入日志
     */
    private void updateImportLog(ExcelImportLog log, BatchProgress progress, String errorMessage) {
        log.setImportStatus(progress.getStatus());
        log.setTotalRows(progress.getTotalRows());
        log.setProcessedRows(progress.getProcessedRows());
        log.setSuccessRows(progress.getSuccessRows());
        log.setFailedRows(progress.getFailedRows());
        log.setErrorMessage(errorMessage);
        log.setCompletedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());

        excelImportLogRepository.updateById(log);
    }

    /**
     * 构建批量处理响应
     */
    private OrderBatchProcessResponse buildBatchResponse(String batchId, BatchProgress progress, int processedCount) {
        OrderBatchProcessResponse response = new OrderBatchProcessResponse();
        response.setBatchId(batchId);
        response.setStatus(progress.getStatus());
        response.setTotalRows(progress.getTotalRows());
        response.setProcessedRows(progress.getProcessedRows());
        response.setSuccessRows(progress.getSuccessRows());
        response.setFailedRows(progress.getFailedRows());
        response.setErrorMessage(progress.getErrorMessage());
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }

    /**
     * 生成批次ID
     */
    private String generateBatchId() {
        return "BATCH_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }


    /**
     * 解析客户ID
     */
    private Long resolveCustomerId(String customerName) {
        // TODO: 根据客户名称解析客户ID
        // 这里可以调用CustomerService来查找或创建客户
        return 1L; // 临时返回默认值
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从Spring Security上下文获取当前用户ID
        UserDetails userDetails = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        String userName = userDetails.getUsername();


        User user = userRepository.findByUsername(userName);


        return user.getId();
    }

    /**
     * 获取当前用户所属公司ID
     */
    private Long getCurrentUserCompanyId() {
        // TODO: 从Spring Security上下文获取当前用户所属公司ID
        return 1L;
    }

    /**
     * 批量处理进度内部类
     */
    private static class BatchProgress {
        private final String batchId;
        private String status = "PROCESSING";
        private int totalRows = 0;
        private int processedRows = 0;
        private int successRows = 0;
        private int failedRows = 0;
        private String errorMessage;
        private final List<String> errors = new ArrayList<>();

        public BatchProgress(String batchId) {
            this.batchId = batchId;
        }

        // Getters and setters
        public String getBatchId() {
            return batchId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getTotalRows() {
            return totalRows;
        }

        public void setTotalRows(int totalRows) {
            this.totalRows = totalRows;
        }

        public int getProcessedRows() {
            return processedRows;
        }

        public void setProcessedRows(int processedRows) {
            this.processedRows = processedRows;
        }

        public int getSuccessRows() {
            return successRows;
        }

        public void setSuccessRows(int successRows) {
            this.successRows = successRows;
        }

        public int getFailedRows() {
            return failedRows;
        }

        public void setFailedRows(int failedRows) {
            this.failedRows = failedRows;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public void incrementProcessedRows() {
            this.processedRows++;
        }

        public void incrementSuccessRows() {
            this.successRows++;
        }

        public void incrementFailedRows() {
            this.failedRows++;
        }

        public void addError(String error) {
            this.errors.add(error);
        }
    }

    /**
     * 订单导入数据内部类
     */
    private static class OrderImportData {
        private String customerName;
        private String deliveryAddress;
        private String expectedDeliveryDate;
        private String remarks;
        private List<OrderItemImportData> orderItems;

        // Getters and setters
        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public void setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
        }

        public String getExpectedDeliveryDate() {
            return expectedDeliveryDate;
        }

        public void setExpectedDeliveryDate(String expectedDeliveryDate) {
            this.expectedDeliveryDate = expectedDeliveryDate;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public List<OrderItemImportData> getOrderItems() {
            return orderItems;
        }

        public void setOrderItems(List<OrderItemImportData> orderItems) {
            this.orderItems = orderItems;
        }
    }

    /**
     * 订单项导入数据内部类
     */
    private static class OrderItemImportData {
        private String productName;
        private int quantity;
        private double unitPrice;

        // Getters and setters
        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
