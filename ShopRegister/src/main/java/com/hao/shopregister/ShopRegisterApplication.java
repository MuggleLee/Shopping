package com.hao.shopregister;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class ShopRegisterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopRegisterApplication.class, args);
    }

}
