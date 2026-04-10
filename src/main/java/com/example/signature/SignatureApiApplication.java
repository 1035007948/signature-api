package com.example.signature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SignatureApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignatureApiApplication.class, args);
    }
}
