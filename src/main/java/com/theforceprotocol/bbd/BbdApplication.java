package com.theforceprotocol.bbd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BbdApplication {

    public static void main(String[] args) {
        SpringApplication.run(BbdApplication.class, args);
    }

}

