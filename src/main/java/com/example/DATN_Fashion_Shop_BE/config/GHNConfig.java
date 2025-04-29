package com.example.DATN_Fashion_Shop_BE.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ghn")
@Data
public class GHNConfig {
    private String token;
    private Integer shopId;
}
