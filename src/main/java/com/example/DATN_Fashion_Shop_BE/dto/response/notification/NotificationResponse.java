package com.example.DATN_Fashion_Shop_BE.dto.response.notification;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.Notification;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse extends BaseResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private String imageUrl;
    private String redirectUrl;
    private Boolean isRead;

    public static NotificationResponse fromNotification(Notification notification, String languageCode) {
        NotificationResponse response = NotificationResponse.builder()
                .id(notification.getId())
                .imageUrl(notification.getImageUrl())
                .message(notification.getTranslationByLanguage(languageCode).getMessage())
                .title(notification.getTranslationByLanguage(languageCode).getTitle())
                .type(notification.getType())
                .redirectUrl(notification.getRedirectUrl())
                .isRead(notification.getIsRead())
                .build();
        response.setCreatedAt(notification.getCreatedAt());
        response.setUpdatedAt(notification.getUpdatedAt());
        response.setCreatedBy(notification.getCreatedBy());
        response.setUpdatedBy(notification.getUpdatedBy());

        return response;
    }
}
