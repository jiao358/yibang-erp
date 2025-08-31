package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.ExcelImportLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Excel导入记录Repository接口
 */
@Mapper
public interface ExcelImportLogRepository extends BaseMapper<ExcelImportLog> {

    /**
     * 根据导入状态查询导入记录列表
     */
    List<ExcelImportLog> selectByImportStatus(@Param("importStatus") String importStatus);

    /**
     * 根据AI处理状态查询导入记录列表
     */
    List<ExcelImportLog> selectByAIProcessingStatus(@Param("aiProcessingStatus") String aiProcessingStatus);

    /**
     * 根据导入用户ID查询导入记录列表
     */
    List<ExcelImportLog> selectByImportUserId(@Param("importUserId") Long importUserId);

    /**
     * 根据文件哈希值查询导入记录
     */
    ExcelImportLog selectByFileHash(@Param("fileHash") String fileHash);

    /**
     * 根据时间范围查询导入记录列表
     */
    List<ExcelImportLog> selectByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
