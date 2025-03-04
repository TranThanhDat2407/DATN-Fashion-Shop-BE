package com.example.DATN_Fashion_Shop_BE.mailing;

import com.example.DATN_Fashion_Shop_BE.model.User;
import org.springframework.web.util.UriComponentsBuilder;

public class AccountVerificationEmailContext extends AbstractEmailContext {
    private String token;
    private String verificationURL; // Biến lưu URL xác nhận

    @Override
    public <T> void init(T context) {
        User user = (User) context;

        put("firstName", user.getFirstName());
        setTemplateLocation("mailing/email-verification");
        setSubject("Complete Your Registration");
        setFrom("kanymuno@gmail.com");
        setTo(user.getEmail());
    }

    public void setToken(String token) {
        this.token = token;
        put("token", token);
    }

    public void buildVerificationUrl(final String baseURL, final String token) {
        this.verificationURL = UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/")
                .queryParam("token", token)
                .toUriString();
        put("verificationURL", verificationURL);
    }

    public String getVerificationUrl() {
        return verificationURL;
    }
}
