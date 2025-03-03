package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.model.Attribute;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.repository.AttributePatternRepository;
import com.example.DATN_Fashion_Shop_BE.repository.AttributeRepository;
import com.example.DATN_Fashion_Shop_BE.repository.AttributeValueRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    public String generateNewSessionId() {
        return UUID.randomUUID().toString();
    }

    public void setSessionIdInCookie(HttpServletResponse response, String sessionId) {
        Cookie sessionCookie = new Cookie("SESSION_ID", sessionId);
//        sessionCookie.setHttpOnly(true);
//        sessionCookie.setSecure(true);
        sessionCookie.setMaxAge(60 * 60 * 24);  // 1 day expiration
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);
    }

    public String getSessionIdFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("SESSION_ID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
