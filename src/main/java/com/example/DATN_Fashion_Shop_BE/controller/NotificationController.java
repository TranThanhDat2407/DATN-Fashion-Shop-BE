package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.notification.NotificationResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.notification.TotalNotificationResponse;
import com.example.DATN_Fashion_Shop_BE.service.NotificationService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications
            (@PathVariable("userId") Long userId,
             @RequestParam (value = "langCode") String langCode,
             @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam(defaultValue = "id") String sortBy,
             @RequestParam(defaultValue = "asc") String sortDir)
    {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<NotificationResponse> res = notificationService.getUserNotifications(userId, langCode, pageable);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
            localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
            PageResponse.fromPage(res)
    ));
    }

    @GetMapping("/total/{userId}")
    public ResponseEntity<ApiResponse<TotalNotificationResponse>> getTotalNotification(
            @PathVariable Long userId)
    {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                notificationService.getNotificationCount(userId)
        ));
    }

    @PutMapping("/mark-all-read/{userId}")
    public ResponseEntity<String> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read.");
    }

    @DeleteMapping("/delete/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotificationById(@PathVariable Long notificationId) {
        notificationService.deleteById(notificationId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification with ID " + notificationId + " has been deleted.");

        return ResponseEntity.ok().body(response);
    }


    @DeleteMapping("/delete/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteNotificationsByUserId(@PathVariable Long userId) {
        notificationService.deleteByUserId(userId);

        // Trả về JSON hợp lệ
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications for user " + userId + " have been deleted.");
        return ResponseEntity.ok(response);
    }

}
