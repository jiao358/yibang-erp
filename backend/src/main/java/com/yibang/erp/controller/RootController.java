package com.yibang.erp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 根路径控制器
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@RestController
public class RootController {
    
    @GetMapping("/")
    public String root() {
        return "亿邦智能供应链协作平台 - 根路径访问成功！";
    }
    
    @GetMapping("/health")
    public String health() {
        return "系统健康检查 - 正常";
    }
}
