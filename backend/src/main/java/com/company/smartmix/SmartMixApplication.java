package com.company.smartmix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * SmartMix 智能生产与成本执行系统
 * 主应用入口
 */
@SpringBootApplication
@EnableJpaAuditing
public class SmartMixApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartMixApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("SmartMix 后端服务启动成功！");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("========================================\n");
    }
}
