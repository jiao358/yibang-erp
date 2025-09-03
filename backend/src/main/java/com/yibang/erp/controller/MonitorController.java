package com.yibang.erp.controller;

import com.yibang.erp.domain.dto.SystemMetricsDTO;
import com.yibang.erp.domain.dto.ServiceStatusDTO;
import com.yibang.erp.domain.dto.DatabaseStatusDTO;
import com.yibang.erp.domain.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统监控控制器
 * 实现按需采集的监控数据获取
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Slf4j
public class MonitorController {

    private final MonitorService monitorService;

    /**
     * 手动采集系统信息
     */
    @PostMapping("/collect-system-info")
    public ResponseEntity<SystemMetricsDTO> collectSystemInfo() {
        try {
            log.info("开始采集系统信息");
            SystemMetricsDTO systemInfo = monitorService.collectSystemInfo();
            log.info("系统信息采集完成");
            return ResponseEntity.ok(systemInfo);
        } catch (Exception e) {
            log.error("采集系统信息失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 手动采集JVM指标
     */
    @PostMapping("/collect-jvm-metrics")
    public ResponseEntity<SystemMetricsDTO> collectJVMMetrics() {
        try {
            log.info("开始采集JVM指标");
            SystemMetricsDTO jvmMetrics = monitorService.collectJVMMetrics();
            log.info("JVM指标采集完成");
            return ResponseEntity.ok(jvmMetrics);
        } catch (Exception e) {
            log.error("采集JVM指标失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 手动采集数据库状态
     */
    @PostMapping("/collect-database-status")
    public ResponseEntity<DatabaseStatusDTO> collectDatabaseStatus() {
        try {
            log.info("开始采集数据库状态");
            DatabaseStatusDTO dbStatus = monitorService.collectDatabaseStatus();
            log.info("数据库状态采集完成");
            return ResponseEntity.ok(dbStatus);
        } catch (Exception e) {
            log.error("采集数据库状态失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取最新采集的数据
     */
    @GetMapping("/get-latest-data")
    public ResponseEntity<SystemMetricsDTO> getLatestData() {
        try {
            log.info("获取最新监控数据");
            SystemMetricsDTO latestData = monitorService.getLatestData();
            return ResponseEntity.ok(latestData);
        } catch (Exception e) {
            log.error("获取最新监控数据失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 轻量级健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("系统监控服务正常");
    }
}
