package com.example.DATN_Fashion_Shop_BE.config;

import com.example.DATN_Fashion_Shop_BE.component.CorsFilter;
import com.example.DATN_Fashion_Shop_BE.component.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.http.HttpMethod.GET;

@Configuration
// Kích hoạt Spring Security trong ứng dụng.
@EnableWebSecurity(debug = true)

// Kích hoạt bảo mật dựa trên annotation ở cấp phương thức. Ví dụ:
//@PreAuthorize: Kiểm tra quyền trước khi thực thi phương thức.
//@PostAuthorize: Kiểm tra quyền sau khi thực thi phương thức.
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
//Bật hỗ trợ Web MVC trong Spring, cần thiết khi làm việc với REST API hoặc các thành phần web.
@EnableWebMvc
@RequiredArgsConstructor
public class WebSercurityConfig {
    @Value("${api.prefix}")
    private String apiPrefix;
    private final JwtTokenFilter jwtTokenFilter;
    private final CorsFilter corsFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)  throws Exception{
        http
                //.addFilterBefore(corsFilter, JwtTokenFilter.class)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(requests -> {
                    requests

                            .requestMatchers(
                                    String.format("%s/users/register", apiPrefix),
                                    String.format("%s/users/login", apiPrefix),
                                    String.format("%s/users/check-phone", apiPrefix),
                                    String.format("%s/users/check-email", apiPrefix),
                                    String.format("%s/users/forgot-password", apiPrefix),
                                    String.format("%s/users/verify-otp", apiPrefix),
                                    String.format("%s/users/reset-password", apiPrefix),
                                    String.format("%s/cart/**", apiPrefix),
                                    String.format("%s/reviews/**", apiPrefix),
                                    String.format("%s/cart/**", apiPrefix),
                                    String.format("%s/languages/**", apiPrefix),
                                    String.format("%s/healthcheck/**", apiPrefix),

                                    "/api-docs",
                                    "/api-docs/**",
                                    "/swagger-resources",
                                    "/swagger-resources/**",
                                    "/configuration/ui",
                                    "/configuration/security",
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/webjars/swagger-ui/**",
                                    "/swagger-ui/index.html"
                            )
                            .permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/roles**", apiPrefix)).permitAll()

                            .requestMatchers(GET, String.format("%s/categories/**", apiPrefix)).permitAll()

                            .requestMatchers(GET,
                                    String.format("%s/products/**", apiPrefix)).permitAll()

                            .requestMatchers(GET,
                                    String.format("%s/orders/**", apiPrefix)).permitAll()

                            .requestMatchers(GET,
                                    String.format("%s/order_details/**", apiPrefix)).permitAll()

                            .anyRequest()

                            .authenticated();

                    //.anyRequest().permitAll();
                })
                //Vô hiệu hóa bảo vệ CSRF.(Cross-Site Request Forgery)
                //Giúp các yêu cầu POST, PUT, DELETE, PATCH không cần CSRF token.
                .csrf(AbstractHttpConfigurer::disable);
//        http.securityMatcher(String.valueOf(EndpointRequest.toAnyEndpoint()));
        return http.build();
    }
}
