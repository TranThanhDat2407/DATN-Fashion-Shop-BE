package com.example.DATN_Fashion_Shop_BE.dto.request.Notification;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class NotificationRequest {
    private Long userId; // Nếu null thì là coupon chung

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Redirect URL is required")
    private String redirectUrl;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotNull(message = "Translations are required")
    private List<NotificationTranslationRequest> translations;
}
