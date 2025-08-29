package com.yibang.erp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {
    RedisRepositoriesAutoConfiguration.class
})
@MapperScan("com.yibang.erp.infrastructure.repository")
public class YibangErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(YibangErpApplication.class, args);
        System.out.println("=================================");
        System.out.println("亿邦智能供应链协作平台启动成功！");
        System.out.println("=================================");
    }
}
