
package com.example.DATN_Fashion_Shop_BE.config;


import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import javax.sql.DataSource;

@Configuration
public class JacksonConfig {
    @Bean
    public Module hibernate5Module() {
        Hibernate5Module hibernate5Module = new Hibernate5Module();
        // Tùy chọn: cấu hình cho module nếu cần (ví dụ, disable force lazy loading)
        // hibernate5Module.disable(Hibernate5Module.Feature.FORCE_LAZY_LOADING);
        return hibernate5Module;
    }
}
