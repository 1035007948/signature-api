/**
 * 签名和验签接口项目 - 主启动类
 *
 * 本项目提供以下功能：
 * 1. 签名接口：对请求数据进行数字签名，返回签名结果
 * 2. 验签接口：验证签名的有效性
 *
 * 技术栈：Spring Boot + JPA + MySQL + Redis + Swagger2
 *
 * @author Developer
 * @version 1.0.0
 */
package com.example.signature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用主启动类
 * @SpringBootApplication 注解整合了 @Configuration、@EnableAutoConfiguration 和 @ComponentScan
 */
@SpringBootApplication
public class SignatureApiApplication {

    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SignatureApiApplication.class, args);
    }

}
