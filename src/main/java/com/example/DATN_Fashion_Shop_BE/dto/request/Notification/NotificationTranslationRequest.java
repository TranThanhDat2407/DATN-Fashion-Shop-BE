package com.example.DATN_Fashion_Shop_BE.dto.request.Notification;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationTranslationRequest {
    @NotBlank(message = "Language code is required")
    private String langCode;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;
}
