package com.example.DATN_Fashion_Shop_BE.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paypal")
@Data
public class PaypalConfig {
    private Client client;
    private String mode;

    @Data
    public static class Client {
        private String id;
        private String secret;
    }
}

