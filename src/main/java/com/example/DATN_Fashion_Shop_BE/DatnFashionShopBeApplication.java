package com.example.DATN_Fashion_Shop_BE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableJpaAuditing
@EnableSpringConfigured
@SpringBootApplication
public class DatnFashionShopBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatnFashionShopBeApplication.class, args);
	}

}
