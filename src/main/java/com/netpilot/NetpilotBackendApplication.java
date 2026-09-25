package com.netpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 添加这一行，启用定时任务
public class NetpilotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetpilotBackendApplication.class, args);
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║   🚀 NetPilot 启动成功！                ║");
        System.out.println("║   访问: http://localhost:8080/api       ║");
        System.out.println("╚═══════════════════════════════════════════╝");
    }
}