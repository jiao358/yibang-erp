package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.ExcelImportLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Excel导入记录Repository接口
 */
@Mapper
public interface ExcelImportLogRepository extends BaseMapper<ExcelImportLog> {

    /**
     * 根据导入状态查询导入记录列表
     */
    @Select("SELECT * FROM excel_import_logs WHERE import_status = #{importStatus} ORDER BY created_at DESC")
    List<ExcelImportLog> selectByImportStatus(@Param("importStatus") String importStatus);

    /**
     * 根据AI处理状态查询导入记录列表
     */
    @Select("SELECT * FROM excel_import_logs WHERE ai_processing_status = #{aiProcessingStatus} ORDER BY created_at DESC")
    List<ExcelImportLog> selectByAIProcessingStatus(@Param("aiProcessingStatus") String aiProcessingStatus);

    /**
     * 根据导入用户ID查询导入记录列表
     */
    @Select("SELECT * FROM excel_import_logs WHERE import_user_id = #{importUserId} ORDER BY created_at DESC")
    List<ExcelImportLog> selectByImportUserId(@Param("importUserId") Long importUserId);

    /**
     * 根据文件哈希值查询导入记录
     */
    @Select("SELECT * FROM excel_import_logs WHERE file_hash = #{fileHash} ORDER BY created_at DESC LIMIT 1")
    ExcelImportLog selectByFileHash(@Param("fileHash") String fileHash);

    /**
     * 根据时间范围查询导入记录列表
     */
    @Select("SELECT * FROM excel_import_logs WHERE created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<ExcelImportLog> selectByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
