package com.yibang.erp.common.util;

import com.yibang.erp.common.request.PageRequest;
import com.yibang.erp.common.response.PageResult;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public class PageUtils {
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> createPageResult(List<T> records, long total, PageRequest pageRequest) {
        return PageResult.of(records, total, pageRequest.getCurrent(), pageRequest.getSize());
    }
    
    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> createEmptyPageResult(PageRequest pageRequest) {
        return PageResult.empty(pageRequest.getCurrent(), pageRequest.getSize());
    }
    
    /**
     * 手动分页（适用于内存中的数据）
     */
    public static <T> PageResult<T> manualPage(List<T> allData, PageRequest pageRequest) {
        int total = allData.size();
        int offset = pageRequest.getOffset();
        int limit = pageRequest.getLimit();
        
        if (offset >= total) {
            return createEmptyPageResult(pageRequest);
        }
        
        int endIndex = Math.min(offset + limit, total);
        List<T> pageData = allData.subList(offset, endIndex);
        
        return createPageResult(pageData, total, pageRequest);
    }
    
    /**
     * 数据转换分页
     */
    public static <T, R> PageResult<R> convertPageResult(PageResult<T> source, Function<T, R> converter) {
        List<R> convertedRecords = source.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        
        return PageResult.of(convertedRecords, source.getTotal(), source.getCurrent(), source.getSize());
    }
    
    /**
     * 验证分页参数
     */
    public static void validatePageRequest(PageRequest pageRequest) {
        if (pageRequest.getCurrent() == null || pageRequest.getCurrent() < 1) {
            pageRequest.setCurrent(1);
        }
        
        if (pageRequest.getSize() == null || pageRequest.getSize() < 1) {
            pageRequest.setSize(20);
        }
        
        if (pageRequest.getSize() > 1000) {
            pageRequest.setSize(1000);
        }
    }
    
    /**
     * 获取总页数
     */
    public static long getTotalPages(long total, long size) {
        return (total + size - 1) / size;
    }
    
    /**
     * 检查是否有下一页
     */
    public static boolean hasNext(long current, long totalPages) {
        return current < totalPages;
    }
    
    /**
     * 检查是否有上一页
     */
    public static boolean hasPrevious(long current) {
        return current > 1;
    }
}
