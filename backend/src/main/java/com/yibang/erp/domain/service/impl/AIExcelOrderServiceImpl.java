package com.yibang.erp.domain.service.impl;

import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yibang.erp.domain.dto.*;
import com.yibang.erp.domain.entity.AIExcelProcessTask;
import com.yibang.erp.domain.entity.AIExcelProcessTaskDetail;
import com.yibang.erp.domain.entity.Order;
import com.yibang.erp.domain.entity.OrderItem;
import com.yibang.erp.domain.service.*;
import com.yibang.erp.infrastructure.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI Excel订单处理服务实现类
 * 完全独立于现有的批量处理服务
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AIExcelOrderServiceImpl extends ServiceImpl<AIExcelProcessTaskRepository, AIExcelProcessTask> implements AIExcelOrderService {

    @Autowired
    private AIExcelProcessTaskRepository taskRepository;

    @Autowired
    private AIExcelProcessTaskDetailRepository taskDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderNumberGeneratorService orderNumberGeneratorService;

    @Autowired
    private ProductMatchingService productMatchingService;

    @Autowired
    private CustomerMatchingService customerMatchingService;

    @Autowired
    private AIExcelErrorOrderService errorOrderService;

    @Autowired
    private AIExcelFieldRecognitionService fieldRecognitionService;

    @Autowired
    private OrderServiceImpl orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 任务进度缓存
    private final Map<String, TaskProgress> progressCache = new ConcurrentHashMap<>();

    // 当前任务ID上下文
    private final ThreadLocal<String> currentTaskIdContext = new ThreadLocal<>();
    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @Override
    public AIExcelProcessResponse startAIExcelProcess(MultipartFile file, AIExcelProcessRequest request) {
        String taskId = generateTaskId();

        try {

            // 创建任务记录
            AIExcelProcessTask task = createTaskRecord(file, request, taskId);
            taskRepository.insert(task);
            log.info("ai-excel-task-save success");
            // 初始化进度缓存
            TaskProgress progress = new TaskProgress(taskId);
            progressCache.put(taskId, progress);


            // 异步启动处理任务
            startAsyncProcessing(file, request, taskId);


            return buildInitialResponse(taskId, task);

        } catch (Exception e) {
            log.error("启动AI Excel处理任务失败", e);
            return buildErrorResponse(taskId, e.getMessage());
        }
    }

    @Override
    public AIExcelProcessResponse getTaskProgress(String taskId) {
        try {
            AIExcelProcessTask task = taskRepository.selectOne(
                    new QueryWrapper<AIExcelProcessTask>().eq("task_id", taskId)
            );

            if (task == null) {
                return buildErrorResponse(taskId, "任务不存在");
            }

            TaskProgress progress = progressCache.get(taskId);
            if (progress == null) {
                progress = new TaskProgress(taskId);
                progressCache.put(taskId, progress);
            }

            return buildProgressResponse(task, progress);

        } catch (Exception e) {
            log.error("获取任务进度失败", e);
            return buildErrorResponse(taskId, e.getMessage());
        }
    }

    @Override
    public boolean cancelTask(String taskId) {
        try {
            AIExcelProcessTask task = taskRepository.selectOne(
                    new QueryWrapper<AIExcelProcessTask>().eq("task_id", taskId)
            );

            if (task == null) {
                return false;
            }

            if ("PROCESSING".equals(task.getTaskStatus())) {
                task.setTaskStatus("CANCELLED");
                task.setUpdatedAt(LocalDateTime.now());
                taskRepository.updateById(task);

                // 更新进度缓存
                TaskProgress progress = progressCache.get(taskId);
                if (progress != null) {
                    progress.setStatus("CANCELLED");
                }

                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("取消任务失败", e);
            return false;
        }
    }

    @Override
    public AIExcelProcessResponse getTaskResult(String taskId) {
        try {
            AIExcelProcessTask task = taskRepository.selectOne(
                    new QueryWrapper<AIExcelProcessTask>().eq("task_id", taskId)
            );

            if (task == null) {
                return buildErrorResponse(taskId, "任务不存在");
            }

            // 获取任务详情
            List<AIExcelProcessTaskDetail> details = taskDetailRepository.selectList(
                    new QueryWrapper<AIExcelProcessTaskDetail>().eq("task_id", taskId)
            );

            return buildResultResponse(task, details);

        } catch (Exception e) {
            log.error("获取任务结果失败", e);
            return buildErrorResponse(taskId, e.getMessage());
        }
    }

    @Override
    public TaskListResponse getUserTasks(Long userId, String status, Integer page, Integer size) {
        try {
            log.info("查询用户任务列表，用户ID: {}, 状态: {}, 页码: {}, 大小: {}", userId, status, page, size);
            
            // 构建查询条件
            QueryWrapper<AIExcelProcessTask> queryWrapper = new QueryWrapper<>();
            
            // 根据用户ID筛选
            queryWrapper.eq("sales_user_id", userId);
            
            // 根据状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("task_status", status);
            }
            
            // 只查询未删除的任务
            queryWrapper.eq("deleted", false);
            
            // 按创建时间倒序排列
            queryWrapper.orderByDesc("created_at");
            
            // 执行分页查询
            Page<AIExcelProcessTask> pageParam = new Page<>(page, size);
            Page<AIExcelProcessTask> result = taskRepository.selectPage(pageParam, queryWrapper);
            
            // 转换为TaskInfo列表
            List<TaskListResponse.TaskInfo> taskInfos = result.getRecords().stream().map(task -> {
                TaskListResponse.TaskInfo info = new TaskListResponse.TaskInfo();
                
                // 基本任务信息
                info.setTaskId(task.getTaskId());
                info.setFileName(task.getFileName());
                info.setStatus(task.getTaskStatus());
                info.setTotalRows(task.getTotalRows());
                info.setSuccessRows(task.getSuccessRows());
                info.setFailedRows(task.getFailedRows());
                info.setManualProcessRows(task.getManualProcessRows());
                info.setFileSize(task.getFileSize());
                
                // 时间信息 - 转换为字符串格式
                if (task.getCreatedAt() != null) {
                    info.setCreatedAt(task.getCreatedAt().toString());
                }
                if (task.getStartedAt() != null) {
                    info.setStartedAt(task.getStartedAt().toString());
                }
                if (task.getCompletedAt() != null) {
                    info.setCompletedAt(task.getCompletedAt().toString());
                }
                
                // 计算处理时间（毫秒）
                if (task.getStartedAt() != null && task.getCompletedAt() != null) {
                    long processingTime = java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis();
                    info.setProcessingTime(processingTime);
                }
                
                // 设置上传用户
                if (task.getCreatedBy() != null) {
                    info.setUploadUser("用户" + task.getCreatedBy());
                }
                
                // 设置供应商信息（暂时设置为默认值）
                info.setSupplier("待设置");
                
                return info;
            }).collect(Collectors.toList());

            long count = taskRepository.selectCount( queryWrapper);
            // 构建响应
            TaskListResponse response = TaskListResponse.success();
            response.setContent(taskInfos);
            response.setTotalElements(count);
            response.setTotalPages((int) result.getPages());
            response.setCurrentPage(page);
            response.setSize(size);
            
            log.info("查询完成，用户ID: {}, 总任务数: {}, 当前页: {}, 每页大小: {}", 
                    userId, result.getTotal(), page, size);
            
            return response;
            
        } catch (Exception e) {
            log.error("查询用户任务列表失败，用户ID: {}", userId, e);
            return TaskListResponse.error("查询任务列表失败: " + e.getMessage());
        }
    }

    @Override
    public TaskStatisticsResponse getTaskStatistics(Long companyId, String startDate, String endDate) {
        try {
            log.info("查询任务统计信息，公司ID: {}, 开始日期: {}, 结束日期: {}", companyId, startDate, endDate);
            
            // 构建基础查询条件
            QueryWrapper<AIExcelProcessTask> baseWrapper = new QueryWrapper<>();
            baseWrapper.eq("deleted", false);
            
            // 根据公司ID筛选
            if (companyId != null) {
                baseWrapper.eq("sales_company_id", companyId);
            }
            
            // 根据时间范围筛选
            if (startDate != null && endDate != null) {
                try {
                    LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
                    LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
                    baseWrapper.between("created_at", startDateTime, endDateTime);
                } catch (Exception e) {
                    log.warn("时间格式解析失败，使用默认时间范围: {} - {}", startDate, endDate);
                }
            }
            
            // 查询总任务数
            Long totalTasks = taskRepository.selectCount(baseWrapper);
            
            // 查询各状态的任务数
            QueryWrapper<AIExcelProcessTask> processingWrapper = new QueryWrapper<>();
            processingWrapper.eq("deleted", false);
            if (companyId != null) {
                processingWrapper.eq("sales_company_id", companyId);
            }
            if (startDate != null && endDate != null) {
                try {
                    LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
                    LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
                    processingWrapper.between("created_at", startDateTime, endDateTime);
                } catch (Exception e) {
                    log.warn("时间格式解析失败，使用默认时间范围: {} - {}", startDate, endDate);
                }
            }
            processingWrapper.eq("task_status", "PROCESSING");
            Long processingTasks = taskRepository.selectCount(processingWrapper);
            
            QueryWrapper<AIExcelProcessTask> completedWrapper = new QueryWrapper<>();
            completedWrapper.eq("deleted", false);
            if (companyId != null) {
                completedWrapper.eq("sales_company_id", companyId);
            }
            if (startDate != null && endDate != null) {
                try {
                    LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
                    LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
                    completedWrapper.between("created_at", startDateTime, endDateTime);
                } catch (Exception e) {
                    log.warn("时间格式解析失败，使用默认时间范围: {} - {}", startDate, endDate);
                }
            }
            completedWrapper.eq("task_status", "COMPLETED");
            Long completedTasks = taskRepository.selectCount(completedWrapper);
            
            QueryWrapper<AIExcelProcessTask> failedWrapper = new QueryWrapper<>();
            failedWrapper.eq("deleted", false);
            if (companyId != null) {
                failedWrapper.eq("sales_company_id", companyId);
            }
            if (startDate != null && endDate != null) {
                try {
                    LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
                    LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
                    failedWrapper.between("created_at", startDateTime, endDateTime);
                } catch (Exception e) {
                    log.warn("时间格式解析失败，使用默认时间范围: {} - {}", startDate, endDate);
                }
            }
            failedWrapper.eq("task_status", "FAILED");
            Long failedTasks = taskRepository.selectCount(failedWrapper);
            
            // 查询今日任务数
            QueryWrapper<AIExcelProcessTask> todayWrapper = new QueryWrapper<>();
            todayWrapper.eq("deleted", false);
            if (companyId != null) {
                todayWrapper.eq("sales_company_id", companyId);
            }
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            todayWrapper.between("created_at", todayStart, todayEnd);
            Long todayTasks = taskRepository.selectCount(todayWrapper);
            
            // 查询本周任务数
            QueryWrapper<AIExcelProcessTask> thisWeekWrapper = new QueryWrapper<>();
            thisWeekWrapper.eq("deleted", false);
            if (companyId != null) {
                thisWeekWrapper.eq("sales_company_id", companyId);
            }
            LocalDateTime weekStart = LocalDateTime.now().with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime weekEnd = LocalDateTime.now().with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            thisWeekWrapper.between("created_at", weekStart, weekEnd);
            Long thisWeekTasks = taskRepository.selectCount(thisWeekWrapper);
            
            // 查询本月任务数
            QueryWrapper<AIExcelProcessTask> thisMonthWrapper = new QueryWrapper<>();
            thisMonthWrapper.eq("deleted", false);
            if (companyId != null) {
                thisMonthWrapper.eq("sales_company_id", companyId);
            }
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().getMonth().length(LocalDateTime.now().toLocalDate().isLeapYear()))
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            thisMonthWrapper.between("created_at", monthStart, monthEnd);
            Long thisMonthTasks = taskRepository.selectCount(thisMonthWrapper);
            
            // 构建响应
            TaskStatisticsResponse response = TaskStatisticsResponse.success();
            response.setTotalTasks(totalTasks);
            response.setProcessingTasks(processingTasks);
            response.setCompletedTasks(completedTasks);
            response.setFailedTasks(failedTasks);
            response.setTodayTasks(todayTasks);
            response.setThisWeekTasks(thisWeekTasks);
            response.setThisMonthTasks(thisMonthTasks);
            
            log.info("统计完成，总任务数: {}, 处理中: {}, 完成: {}, 失败: {}, 今日: {}, 本周: {}, 本月: {}", 
                    totalTasks, processingTasks, completedTasks, failedTasks, todayTasks, thisWeekTasks, thisMonthTasks);
            
            return response;
            
        } catch (Exception e) {
            log.error("查询任务统计信息失败", e);
            return TaskStatisticsResponse.error("查询统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 异步启动处理任务
     */
    @Async
    protected void startAsyncProcessing(MultipartFile file, AIExcelProcessRequest request, String taskId) {
        try {
            // 设置当前任务ID到线程上下文
            setCurrentTaskId(taskId);

            log.info("AI Excel处理任务开始，任务ID: {} {}", taskId, file.getName());
            // 更新任务状态为处理中
            updateTaskStatus(taskId, "PROCESSING", "开始处理Excel文件");

            // 解析Excel文件  解析里面有row的处理
            List<AIExcelRowData> rowDataList = parseExcelFile(file, taskId);

            // AI字段识别和映射
            List<AIExcelRowData> recognizedDataList = recognizeFields(rowDataList, taskId);

            // 数据验证和商品匹配
            List<ProcessedRowData> processedDataList = processAndValidateData(recognizedDataList, request, taskId);



            // 创建订单  这里面订单号 得处理一下，需要记录原来平台的订单号
            createOrdersFromProcessedData(processedDataList, request, taskId);

            // 更新任务状态为完成
            updateTaskStatus(taskId, "COMPLETED", "Excel处理完成");

        } catch (Exception e) {
            log.error("AI Excel处理任务执行失败", e);
            updateTaskStatus(taskId, "FAILED", "处理失败: " + e.getMessage());
        } finally {
            // 清理当前任务ID上下文
            clearCurrentTaskId();
        }
    }

    /**
     * 解析Excel文件 - 使用EasyExcel标准方式
     */
    private List<AIExcelRowData> parseExcelFile(MultipartFile file, String taskId) throws IOException {
        List<AIExcelRowData> rowDataList = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        log.info("开始解析Excel文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                private int rowIndex = 0;
                private int processedRows = 0;

                private int exceptionRow=0;

                private int totalRows=0;
                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    log.info("Excel 真标题行：", headMap);
                    headers.addAll(headMap.values());
                    log.info("Excel标题行: {}", headers);
                }

                @Override
                public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
                    try {

                        // 数据行
                        AIExcelRowData row = new AIExcelRowData();
                        row.setRowNumber(rowIndex);
                        totalRows++;

                        // 提取原始值并过滤空值
                        List<String> rawValues = new ArrayList<>();
                        for (int i = 0; i < headers.size(); i++) {
                            String value = rowData.get(i);
                            rawValues.add(value != null ? value.trim() : "");
                        }
                        row.setRawValues(rawValues);

                        // 构建列标题映射（列索引 -> 列标题）
                        Map<String, String> columnHeaders = new HashMap<>();
                        for (int i = 0; i < headers.size(); i++) {
                            columnHeaders.put(String.valueOf(i), headers.get(i));
                        }
                        row.setColumnHeaders(columnHeaders);

                        rowDataList.add(row);
                        processedRows++;

                        // 每处理100行更新一次进度
                        if (processedRows % 100 == 0) {
                            updateTaskProgress(taskId, processedRows, null, null, 0, 0);
                            log.debug("已处理 {} 行数据", processedRows);
                        }

                        rowIndex++;

                    } catch (Exception e) {
                        log.error("解析第 {} 行数据失败", rowIndex, e);
                        exceptionRow++;
                        // 记录错误但不中断整个解析过程
                        AIExcelProcessTaskDetail    aiExcelProcessTaskDetail = new AIExcelProcessTaskDetail();
                        aiExcelProcessTaskDetail.setTaskId(taskId);
                        aiExcelProcessTaskDetail.setExcelRowNumber(rowIndex);
                        aiExcelProcessTaskDetail.setProcessStatus("ERROR");
                        aiExcelProcessTaskDetail.setRawData(new JSONObject(rowData).toString());
                        aiExcelProcessTaskDetail.setProcessResult("解析失败");
                        aiExcelProcessTaskDetail.setConfidence(0.0);
                        aiExcelProcessTaskDetail.setErrorType("PARSE_ERROR");
                        aiExcelProcessTaskDetail.setErrorMessage(e.getMessage());
                        taskDetailRepository.insert(aiExcelProcessTaskDetail);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 最终更新任务总行数
                    updateTaskProgress(taskId, totalRows, null, null, exceptionRow, null);

                    log.info("Excel文件解析完成，共处理 {} 行数据", processedRows);
                }
            }).sheet().doRead();

        } catch (Exception e) {
            log.error("Excel文件解析失败: {}", file.getOriginalFilename(), e);
            throw new IOException("Excel文件解析失败: " + e.getMessage(), e);
        }

        return rowDataList;
    }

    /**
     * AI字段识别和映射
     */
    private List<AIExcelRowData> recognizeFields(List<AIExcelRowData> rowDataList, String taskId) {
        List<AIExcelRowData> recognizedDataList = new ArrayList<>();

        for (AIExcelRowData rowData : rowDataList) {
            try {
                // 使用AI模型识别字段
                log.info("开始AI字段识别，行号: {}", rowData.getRowNumber());
                AIExcelRowData.RecognizedFields fields = fieldRecognitionService.recognizeRowFields(rowData);
                rowData.setRecognizedFields(fields);

                double confidence = fields.getAiConfidence();
                //先匹配AI返回的置信度，如果没有 则进行规则匹配计算
                if(fields.getAiConfidence() ==null || fields.getAiConfidence()==0){
                    // 计算置信度
                     confidence = calculateConfidence(fields);
                    rowData.setConfidence(confidence);
                }


                log.info("AI字段识别完成，行号: {}, 置信度: {}, 识别字段数: {}",
                        rowData.getRowNumber(), confidence,
                        fields != null ? getRecognizedFieldsCount(fields) : 0);

                recognizedDataList.add(rowData);

                // 更新进度 这里有问题
                updateTaskProgress(taskId, null, 1, null, null, null);

            } catch (Exception e) {
                log.error("字段识别失败，行号: {}", rowData.getRowNumber(), e);
                // 记录错误详情
                saveTaskDetail(taskId, rowData.getRowNumber(), "FAILED",
                        "字段识别失败", e.getMessage(), null, 0.0);

                // 保存错误订单信息
                try {
                    Map<String, Object> rawData = new HashMap<>();
                    rawData.put("excelRowNumber", rowData.getRowNumber());
                    rawData.put("rawValues", rowData.getRawValues());
                    rawData.put("columnHeaders", rowData.getColumnHeaders());

                    errorOrderService.saveErrorOrder(
                            taskId,
                            rowData.getRowNumber(),
                            rawData,
                            "VALIDATION_ERROR",
                            "字段识别失败: " + e.getMessage(),
                            "请检查Excel数据格式，确保列标题和数据内容正确"
                    );
                } catch (Exception saveError) {
                    log.error("保存错误订单信息失败", saveError);
                }
            }finally {
                updateTaskProgress(taskId, null, 1, null, 1, null);
            }
        }

        return recognizedDataList;
    }


    /**
     * 计算识别的字段数量
     */
    private int getRecognizedFieldsCount(AIExcelRowData.RecognizedFields fields) {
        if (fields == null) return 0;

        int count = 0;
        if (fields.getCustomerName() != null) count++;
        if (fields.getCustomerCode() != null) count++;
        if (fields.getContactPerson() != null) count++;
        if (fields.getContactPhone() != null) count++;
        if (fields.getDeliveryAddress() != null) count++;
        if (fields.getExpectedDeliveryDate() != null) count++;
        if (fields.getProductSku() != null) count++;
        if (fields.getProductName() != null) count++;
        if (fields.getProductSpecification() != null) count++;
        if (fields.getQuantity() != null) count++;
        if (fields.getUnitPrice() != null) count++;
        if (fields.getUnit() != null) count++;
        if (fields.getOrderType() != null) count++;
        if (fields.getSpecialRequirements() != null) count++;
        if (fields.getRemarks() != null) count++;

        return count;
    }

    /**
     * 计算置信度
     */
    private double calculateConfidence(AIExcelRowData.RecognizedFields fields) {
        double confidence = 0.0;
        int totalFields = 0;
        int recognizedFields = 0;

        // 检查关键字段
        if (fields.getCustomerName() != null) {
            recognizedFields++;
            totalFields++;
        }
        if (fields.getProductName() != null || fields.getProductSku() != null) {
            recognizedFields++;
            totalFields++;
        }
        if (fields.getQuantity() != null) {
            recognizedFields++;
            totalFields++;
        }
        if (fields.getUnitPrice() != null) {
            recognizedFields++;
            totalFields++;
        }

        if (totalFields > 0) {
            confidence = (double) recognizedFields / totalFields;
        }

        return confidence;
    }

    /**
     * 数据验证和商品匹配
     */
    private List<ProcessedRowData> processAndValidateData(List<AIExcelRowData> recognizedDataList,
                                                          AIExcelProcessRequest request, String taskId) {
        List<ProcessedRowData> processedDataList = new ArrayList<>();

        for (AIExcelRowData rowData : recognizedDataList) {
            try {
                ProcessedRowData processedData = new ProcessedRowData();
                processedData.setRowNumber(rowData.getRowNumber());
                processedData.setRawData(rowData);

                // 验证数据完整性
                if (!validateRowData(rowData)) {
                    processedData.setStatus("FAILED");
                    processedData.setErrorMessage("数据不完整");
                    processedData.setConfidence(rowData.getConfidence()==null?0.0:Double.valueOf(rowData.getConfidence()));
                    processedDataList.add(processedData);
                    continue;
                }

                // 商品匹配  只要商品匹配成功即可
                ProductMatchResult productMatch = matchProduct(rowData, request);
                processedData.setProductMatch(productMatch);

//                // 客户匹配 不需要匹配
//                CustomerMatchResult customerMatch = matchCustomer(rowData, request);
//                processedData.setCustomerMatch(customerMatch);

                 // 确定处理状态    && customerMatch.isMatched(){//

                if (productMatch.isMatched() ) {
                    processedData.setStatus("SUCCESS");
                    processedData.setConfidence(productMatch.getConfidence());
                } else if (productMatch.getConfidence() >= request.getMinConfidenceThreshold() ) {
                    processedData.setStatus("MANUAL_PROCESS");
                    processedData.setConfidence(productMatch.getConfidence());
                } else {
                    processedData.setStatus("FAILED");
                    processedData.setErrorMessage("商品匹配失败");
                    processedData.setConfidence(productMatch.getConfidence());
                }

                processedDataList.add(processedData);

                // 更新进度
                updateTaskProgress(taskId, null, 0, 1, 0, 0);

            } catch (Exception e) {
                log.error("数据处理失败，行号: {}", rowData.getRowNumber(), e);
                ProcessedRowData processedData = new ProcessedRowData();
                processedData.setRowNumber(rowData.getRowNumber());
                processedData.setStatus("FAILED");
                processedData.setErrorMessage("数据处理异常: " + e.getMessage());
                processedData.setConfidence(0.0);
                processedDataList.add(processedData);
            }
        }

        return processedDataList;
    }

    /**
     * 创建订单
     */
    private void createOrdersFromProcessedData(List<ProcessedRowData> processedDataList,
                                               AIExcelProcessRequest request, String taskId) {
        int successCount = 0;
        int failedCount = 0;
        int manualProcessCount = 0;

        for (ProcessedRowData processedData : processedDataList) {
            try {
                if ("SUCCESS".equals(processedData.getStatus())) {
                    // 创建订单
                    Order order = createOrderFromProcessedData(processedData, request);
                    if(order ==null ){
                        //无商品信息 直接失败异常处理
                        failedCount++;

                        // 保存失败详情
                        saveTaskDetail(taskId, processedData.getRowNumber(), "FAILED",
                                "处理失败","没有解析到任何商品信息", null, processedData.getConfidence());
                        continue;

                    }
                    orderRepository.insert(order);

                    // 创建订单项
                    createOrderItems(order.getId(), processedData);

                    successCount++;

                    //order 金额计算
                    orderServiceImpl.calculateOrderTotal(order.getId());
                    // 保存成功详情
                    saveTaskDetail(taskId, processedData.getRowNumber(), "SUCCESS",
                            "订单创建成功", null, order.getId(), processedData.getConfidence());

                } else if ("MANUAL_PROCESS".equals(processedData.getStatus())) {
                    manualProcessCount++;

                    // 保存需要人工处理的详情
                    saveTaskDetail(taskId, processedData.getRowNumber(), "MANUAL_PROCESS",
                            "需要人工处理", processedData.getErrorMessage(), null, processedData.getConfidence());

                } else {
                    failedCount++;

                    // 保存失败详情
                    saveTaskDetail(taskId, processedData.getRowNumber(), "FAILED",
                            "处理失败", processedData.getErrorMessage(), null, processedData.getConfidence());
                }

            } catch (Exception e) {
                log.error("创建订单失败，行号: {}", processedData.getRowNumber(), e);
                failedCount++;

                // 保存错误详情
                saveTaskDetail(taskId, processedData.getRowNumber(), "FAILED",
                        "创建订单异常", e.getMessage(), null, 0.0);
            }
        }

        // 更新最终进度
        updateTaskFinalProgress(taskId, null, Integer.valueOf(0), Integer.valueOf(successCount), Integer.valueOf(failedCount), Integer.valueOf(manualProcessCount));
    }


    private boolean validateRowData(AIExcelRowData rowData) {
        AIExcelRowData.RecognizedFields fields = rowData.getRecognizedFields();
        return fields != null &&
                (fields.getProductName() != null || fields.getProductSku() != null) &&
                fields.getQuantity() != null &&
                fields.getDistrictName() !=null &&
                fields.getDeliveryAddress() !=null &&
                fields.getContactPhone()!=null
                ;
    }

    private ProductMatchResult matchProduct(AIExcelRowData rowData, AIExcelProcessRequest request) {
        try {
            AIExcelRowData.RecognizedFields fields = rowData.getRecognizedFields();
            if (fields == null) {
                return createNoProductMatchResult("字段识别失败");
            }

            // 使用商品匹配服务进行智能匹配
            ProductMatchResult result = productMatchingService.smartMatch(
                    fields.getProductSku(),
                    fields.getProductName()
            );

            if (result.isMatched()) {
                log.info("商品匹配成功，行号: {}, 商品: {}, 置信度: {}",
                        rowData.getRowNumber(), result.getProductName(), result.getConfidence());
            } else {
                log.warn("商品匹配失败，行号: {}, 原因: {}",
                        rowData.getRowNumber(), result.getMatchReason());

                // 保存商品匹配失败的错误信息
                try {
                    Map<String, Object> rawData = new HashMap<>();
                    rawData.put("productSku", fields.getProductSku());
                    rawData.put("productName", fields.getProductName());
                    rawData.put("excelRowNumber", rowData.getRowNumber());
                    rawData.put("matchReason", result.getMatchReason());

                    // 获取任务ID（这里需要从上下文获取）
                    String taskId = getCurrentTaskId(); // 需要实现这个方法
                    if (taskId != null) {
                        errorOrderService.saveErrorOrder(
                                taskId,
                                rowData.getRowNumber(),
                                rawData,
                                "PRODUCT_NOT_FOUND",
                                "商品匹配失败: " + result.getMatchReason(),
                                "请检查商品SKU或名称，或联系管理员添加商品信息"
                        );

                        //todo 这里应该对  ai_excel_process_task_details 表进行插入数据的操作
                    }
                } catch (Exception saveError) {
                    log.error("保存错误订单信息失败", saveError);
                }
            }

            return result;

        } catch (Exception e) {
            log.error("商品匹配异常，行号: {}", rowData.getRowNumber(), e);
            return createNoProductMatchResult("商品匹配异常: " + e.getMessage());
        }
    }

    private CustomerMatchResult matchCustomer(AIExcelRowData rowData, AIExcelProcessRequest request) {
        try {
            AIExcelRowData.RecognizedFields fields = rowData.getRecognizedFields();
            if (fields == null) {
                return createNoCustomerMatchResult("字段识别失败");
            }

            // 使用客户匹配服务进行智能匹配
            CustomerMatchResult result = customerMatchingService.smartMatch(
                    fields.getCustomerCode(),
                    fields.getCustomerName(),
                    fields.getContactPhone()
            );

            if (result.isMatched()) {
                log.info("客户匹配成功，行号: {}, 客户: {}, 置信度: {}",
                        rowData.getRowNumber(), result.getCustomerName(), result.getConfidence());
            } else {
                log.warn("客户匹配失败，行号: {}, 原因: {}",
                        rowData.getRowNumber(), result.getMatchReason());
            }

            return result;

        } catch (Exception e) {
            log.error("客户匹配异常，行号: {}", rowData.getRowNumber(), e);
            return createNoCustomerMatchResult("客户匹配异常: " + e.getMessage());
        }
    }

    private Order createOrderFromProcessedData(ProcessedRowData processedData, AIExcelProcessRequest request) {
        try {
            AIExcelRowData rowData = processedData.getRawData();
            AIExcelRowData.RecognizedFields fields = rowData.getRecognizedFields();

            Order order = new Order();
            // 设置商品信息
            if (!processedData.getProductMatch().isMatched()) {
                //如果没有商品信息的订单应该直接判断失败

                ProductMatchResult productMatch = processedData.getProductMatch();
                order.setSupplierCompanyId(productMatch.getSupplierCompanyId());
                return null;
            }
            // 生成订单号 todo 生成的单号应该使用
            String orderNo = orderNumberGeneratorService.generatePlatformOrderNo(request.getOperatorName(), "EXCEL_IMPORT");
            order.setPlatformOrderId(orderNo);

            // 设置平台订单号（如果有的话）
            if (StringUtils.hasText(fields.getSourceOrderId())) {
                // 这里可以设置其他字段，比如备注
                order.setSourceOrderId(fields.getSourceOrderId());
                order.setRemarks(fields.getRemarks());
            }
            ProductMatchResult productMatch = processedData.getProductMatch();

            log.warn("订单信息 {}",fields);
            //todo customer 客户优先0L
            order.setCustomerId(0L);
            order.setDeliveryAddress(fields.getDeliveryAddress());
            order.setDistrictName(fields.getDistrictName());
            order.setProvinceName(fields.getProvinceName());
            order.setCityName(fields.getCityName());
            order.setDeliveryPhone(fields.getContactPhone());
            order.setSalesId(request.getSalesUserId());
            order.setSupplierCompanyId(fields.getCompanyId());
            order.setSupplierCompanyId(productMatch.getSupplierCompanyId());
            order.setDeliveryContact(fields.getContactPerson());

            order.setExpectedDeliveryDate(LocalDate.now().plusDays(2));
            order.setSpecialRequirements(fields.getRemarks());


            // 设置订单基本信息
            order.setOrderStatus("DRAFT");
            order.setOrderType("NORMAL");
            order.setOrderSource("EXCEL_IMPORT");



            // 设置交货信息
            if (StringUtils.hasText(fields.getExpectedDeliveryDate())) {
                try {
                    // 尝试解析日期，这里简化处理
                    order.setExpectedDeliveryDate(LocalDate.now().plusDays(7));
                } catch (Exception e) {
                    log.warn("日期解析失败，使用默认值: {}", fields.getExpectedDeliveryDate());
                    order.setExpectedDeliveryDate(LocalDate.now().plusDays(7));
                }
            }

            // 设置AI处理信息
            order.setAiProcessed(true);
            order.setAiConfidence(BigDecimal.valueOf(processedData.getConfidence()));

            // 设置扩展字段
            Map<String, Object> extendedFields = new HashMap<>();
            extendedFields.put("excelRowNumber", rowData.getRowNumber());
            extendedFields.put("importTime", LocalDateTime.now());
            extendedFields.put("importSource", "AI_EXCEL");

            if (StringUtils.hasText(fields.getSpecialRequirements())) {
                extendedFields.put("specialRequirements", fields.getSpecialRequirements());
            }
            if (StringUtils.hasText(fields.getRemarks())) {
                extendedFields.put("remarks", fields.getRemarks());
            }

            order.setExtendedFieldsMap(extendedFields);

            // 设置公司信息
            order.setCreatedBy(request.getSalesUserId());
            order.setUpdatedBy(request.getSalesUserId());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            log.info("创建订单成功，行号: {}, 订单号: {}",
                    rowData.getRowNumber(), orderNo);

            return order;

        } catch (Exception e) {
            log.error("创建订单失败，行号: {}", processedData.getRowNumber(), e);
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    private void createOrderItems(Long orderId, ProcessedRowData processedData) {
        try {
            AIExcelRowData rowData = processedData.getRawData();
            AIExcelRowData.RecognizedFields fields = rowData.getRecognizedFields();

            if (processedData.getProductMatch().isMatched()) {
                ProductMatchResult productMatch = processedData.getProductMatch();

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(productMatch.getProductId());
                orderItem.setProductName(productMatch.getProductName());
                orderItem.setSku(productMatch.getSku());
                orderItem.setProductSpecifications(productMatch.getSpecification());

                // 设置数量和价格
                if (fields.getQuantity() != null) {
                    orderItem.setQuantity(fields.getQuantity());
                } else {
                    orderItem.setQuantity(1); // 默认数量为1
                }

                if (fields.getUnitPrice() != null) {
                    orderItem.setUnitPrice(BigDecimal.valueOf(fields.getUnitPrice()));
                    // 计算总价
                    BigDecimal totalPrice = BigDecimal.valueOf(fields.getUnitPrice())
                            .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                    orderItem.setSubtotal(totalPrice);
                } else {
                    orderItem.setUnitPrice(BigDecimal.ZERO);
                    orderItem.setSubtotal(BigDecimal.ZERO);
                }

                // 设置单位
                if (StringUtils.hasText(fields.getUnit())) {
                    orderItem.setUnit(fields.getUnit());
                } else {
                    orderItem.setUnit("件"); // 默认单位
                }

                // 设置扩展信息
                Map<String, Object> extendedFields = new HashMap<>();
                extendedFields.put("aiProcessed", true);
                extendedFields.put("aiConfidence", processedData.getConfidence());
                extendedFields.put("excelRowNumber", rowData.getRowNumber());
                extendedFields.put("importTime", LocalDateTime.now());

                orderItem.setExtendedFieldsMap(extendedFields);

                // 设置基本信息
                orderItem.setCreatedAt(LocalDateTime.now());
                orderItem.setUpdatedAt(LocalDateTime.now());

                // 保存订单项
                orderItemRepository.insert(orderItem);

                log.info("创建订单项成功，行号: {}, 商品: {}, 数量: {}",
                        rowData.getRowNumber(), productMatch.getProductName(), orderItem.getQuantity());
            } else {
                log.warn("无法创建订单项，商品匹配失败，行号: {}", rowData.getRowNumber());
            }

        } catch (Exception e) {
            log.error("创建订单项失败，行号: {}", processedData.getRowNumber(), e);
            throw new RuntimeException("创建订单项失败: " + e.getMessage());
        }
    }

    // 其他必要的辅助方法...
    private String generateTaskId() {
        return "AI_EXCEL_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private AIExcelProcessTask createTaskRecord(MultipartFile file, AIExcelProcessRequest request, String taskId) {
        AIExcelProcessTask task = new AIExcelProcessTask();
        task.setTaskId(taskId);
        task.setFileName(file.getOriginalFilename());
        task.setFileSize(file.getSize());
        task.setFileHash(generateFileHash(file));
        task.setTaskStatus("PENDING");
        task.setSalesUserId(request.getSalesUserId());
        task.setSalesCompanyId(request.getSalesCompanyId());
        task.setPriority(request.getPriority());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setCreatedBy(request.getSalesUserId());
        task.setUpdatedBy(request.getSalesUserId());
        return task;
    }

    private String generateFileHash(MultipartFile file) {
        // 简单的文件哈希生成
        return file.getOriginalFilename() + "_" + file.getSize() + "_" + file.getContentType();
    }

    private void updateTaskStatus(String taskId, String status, String message) {
        try {
            AIExcelProcessTask task = taskRepository.selectOne(
                    new QueryWrapper<AIExcelProcessTask>().eq("task_id", taskId)
            );
            if (task != null) {
                task.setTaskStatus(status);
                task.setErrorMessage(message);
                task.setUpdatedAt(LocalDateTime.now());
                if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                    task.setCompletedAt(LocalDateTime.now());
                } else if ("PROCESSING".equals(status)) {
                    task.setStartedAt(LocalDateTime.now());
                }
                taskRepository.updateById(task);
            }
        } catch (Exception e) {
            log.error("更新任务状态失败", e);
        }
    }

    private void updateTaskFinalProgress(String taskId, Integer totalRows, Integer processedRows,
                                    Integer successRows, Integer failedRows, Integer manualProcessRows){
        try {
            TaskProgress progress = progressCache.get(taskId);
            if (progress != null) {
                if (totalRows != null) progress.setTotalRows(totalRows);
                if (processedRows != null) progress.setProcessedRows(processedRows);
                if (successRows != null) progress.setSuccessRows( successRows);
                if (failedRows != null) progress.setFailedRows(failedRows);
                if (manualProcessRows != null)
                    progress.setManualProcessRows( manualProcessRows);
            }

            //对task进行更新
            AIExcelProcessTask task = taskRepository.selectOne(
                    new QueryWrapper<AIExcelProcessTask>().eq("task_id", taskId)
            );

            task.setTaskStatus("PROCESSING");
            task.setUpdatedAt(LocalDateTime.now());
            task.setProcessedRows(progress.getProcessedRows());
            task.setTotalRows(progress.getTotalRows());
            task.setFailedRows(progress.getFailedRows());
            task.setSuccessRows(progress.getSuccessRows());
            taskRepository.updateById(task);



        } catch (Exception e) {
            log.error("更新任务进度失败", e);
        }
    }
    private void updateTaskProgress(String taskId, Integer totalRows, Integer processedRows,
                                    Integer successRows, Integer failedRows, Integer manualProcessRows) {
        try {
            TaskProgress progress = progressCache.get(taskId);
            if (progress != null) {
                if (totalRows != null) progress.setTotalRows(totalRows);
                if (processedRows != null) progress.setProcessedRows(progress.getProcessedRows() + processedRows);
                if (successRows != null) progress.setSuccessRows(progress.getSuccessRows() + successRows);
                if (failedRows != null) progress.setFailedRows(progress.getFailedRows() + failedRows);
                if (manualProcessRows != null)
                    progress.setManualProcessRows(progress.getManualProcessRows() + manualProcessRows);
            }

            //对task进行更新
            AIExcelProcessTask task = taskRepository.selectOne(
                    new QueryWrapper<AIExcelProcessTask>().eq("task_id", taskId)
            );
            task.setTaskStatus("PROCESSING");
            task.setUpdatedAt(LocalDateTime.now());
            task.setProcessedRows(progress.getProcessedRows());
            task.setTotalRows(progress.getTotalRows());
            task.setFailedRows(progress.getFailedRows());
            taskRepository.updateById(task);



        } catch (Exception e) {
            log.error("更新任务进度失败", e);
        }
    }

    private void saveTaskDetail(String taskId, Integer rowNumber, String status, String message,
                                String errorMessage, Long orderId, Double confidence) {
        try {
            AIExcelProcessTaskDetail detail = new AIExcelProcessTaskDetail();
            detail.setTaskId(taskId);
            detail.setExcelRowNumber(rowNumber);
            detail.setProcessStatus(status);
            detail.setProcessingNotes(message);
            detail.setErrorMessage(errorMessage);
            detail.setConfidence(confidence);
            detail.setCreatedAt(LocalDateTime.now());
            detail.setUpdatedAt(LocalDateTime.now());
            taskDetailRepository.insert(detail);
        } catch (Exception e) {
            log.error("保存任务详情失败", e);
        }
    }

    private AIExcelProcessResponse buildInitialResponse(String taskId, AIExcelProcessTask task) {
        AIExcelProcessResponse response = new AIExcelProcessResponse();
        response.setTaskId(taskId);
        response.setStatus("PENDING");
        response.setMessage("任务已创建，正在启动处理");
        response.setCreatedAt(task.getCreatedAt());
        return response;
    }

    private AIExcelProcessResponse buildProgressResponse(AIExcelProcessTask task, TaskProgress progress) {
        AIExcelProcessResponse response = new AIExcelProcessResponse();
        response.setTaskId(task.getTaskId());
        response.setStatus(task.getTaskStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setStartedAt(task.getStartedAt());
        response.setCompletedAt(task.getCompletedAt());

        if (progress != null) {
            AIExcelProcessResponse.ProgressInfo progressInfo = new AIExcelProcessResponse.ProgressInfo();
            progressInfo.setTotalRows(progress.getTotalRows());
            progressInfo.setProcessedRows(progress.getProcessedRows());
            progressInfo.setSuccessRows(progress.getSuccessRows());
            progressInfo.setFailedRows(progress.getFailedRows());
            progressInfo.setManualProcessRows(progress.getManualProcessRows());
            if (progress.getTotalRows() > 0) {
                progressInfo.setPercentage((double) progress.getProcessedRows() / progress.getTotalRows() * 100);
            }
            response.setProgress(progressInfo);
        }

        return response;
    }

    private AIExcelProcessResponse buildResultResponse(AIExcelProcessTask task, List<AIExcelProcessTaskDetail> details) {
        AIExcelProcessResponse response = new AIExcelProcessResponse();
        response.setTaskId(task.getTaskId());
        response.setStatus(task.getTaskStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setStartedAt(task.getStartedAt());
        response.setCompletedAt(task.getCompletedAt());

        // 构建统计信息
        AIExcelProcessResponse.ProcessStatistics statistics = new AIExcelProcessResponse.ProcessStatistics();
        statistics.setTotalOrders(task.getTotalRows());
        statistics.setSuccessOrders(task.getSuccessRows());
        statistics.setFailedOrders(task.getFailedRows());
        statistics.setManualProcessOrders(task.getManualProcessRows());
        response.setStatistics(statistics);

        return response;
    }

    private AIExcelProcessResponse buildErrorResponse(String taskId, String errorMessage) {
        AIExcelProcessResponse response = new AIExcelProcessResponse();
        response.setTaskId(taskId);
        response.setStatus("FAILED");
        response.setMessage(errorMessage);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    // 内部类
    private static class TaskProgress {
        private String taskId;
        private String status;
        private int totalRows;
        private int processedRows;
        private int successRows;
        private int failedRows;
        private int manualProcessRows;

        public TaskProgress(String taskId) {
            this.taskId = taskId;
            this.status = "PENDING";
        }

        // Getters and setters
        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
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

        public int getManualProcessRows() {
            return manualProcessRows;
        }

        public void setManualProcessRows(int manualProcessRows) {
            this.manualProcessRows = manualProcessRows;
        }
    }

    private static class ProcessedRowData {
        private int rowNumber;
        private AIExcelRowData rawData;
        private String status;
        private String errorMessage;
        private double confidence;
        private ProductMatchResult productMatch;
        private CustomerMatchResult customerMatch;

        // Getters and setters
        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public AIExcelRowData getRawData() {
            return rawData;
        }

        public void setRawData(AIExcelRowData rawData) {
            this.rawData = rawData;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public ProductMatchResult getProductMatch() {
            return productMatch;
        }

        public void setProductMatch(ProductMatchResult productMatch) {
            this.productMatch = productMatch;
        }

        public CustomerMatchResult getCustomerMatch() {
            return customerMatch;
        }

        public void setCustomerMatch(CustomerMatchResult customerMatch) {
            this.customerMatch = customerMatch;
        }
    }


    /**
     * 创建商品匹配失败的结果
     */
    private ProductMatchResult createNoProductMatchResult(String reason) {
        ProductMatchResult result = new ProductMatchResult();
        result.setMatched(false);
        result.setConfidence(0.0);
        result.setMatchReason(reason);
        result.setSuggestion("请检查商品信息是否正确，或联系管理员添加新商品");
        return result;
    }

    /**
     * 创建客户匹配失败的结果
     */
    private CustomerMatchResult createNoCustomerMatchResult(String reason) {
        CustomerMatchResult result = new CustomerMatchResult();
        result.setMatched(false);
        result.setConfidence(0.0);
        result.setMatchReason(reason);
        result.setSuggestion("请检查客户信息是否正确，或联系管理员添加新客户");
        return result;
    }

    /**
     * 设置当前任务ID到线程上下文
     */
    private void setCurrentTaskId(String taskId) {
        currentTaskIdContext.set(taskId);
    }

    /**
     * 获取当前任务ID
     */
    private String getCurrentTaskId() {
        return currentTaskIdContext.get();
    }

    /**
     * 清理当前任务ID上下文
     */
    private void clearCurrentTaskId() {
        currentTaskIdContext.remove();
    }
}
