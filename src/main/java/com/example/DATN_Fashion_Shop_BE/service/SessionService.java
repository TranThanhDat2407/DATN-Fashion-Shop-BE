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
   // Tạo session ID mới và lưu vào cookie
    public String createSession(HttpServletResponse response) {
        String sessionId = UUID.randomUUID().toString();  // Tạo session ID mới
        Cookie cookie = new Cookie("SESSION_ID", sessionId);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(3600);  // Thời gian sống của session (1 giờ)
        cookie.setPath("/");     // Cookie có hiệu lực cho toàn bộ ứng dụng
        response.addCookie(cookie); // Thêm cookie vào response để trình duyệt lưu

        return sessionId;
    }

    // Lấy session ID từ cookie
    public String getSessionIdFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("SESSION_ID".equals(cookie.getName())) {
                    return cookie.getValue();  // Trả về session ID từ cookie
                }
            }
        }
        return null;  // Nếu không có session ID, trả về null
    }
}
