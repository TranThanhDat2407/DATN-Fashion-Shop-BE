package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.request.Notification.NotificationTranslationRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.notification.NotificationResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.notification.TotalNotificationResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.LanguageRepository;
import com.example.DATN_Fashion_Shop_BE.repository.NotificationRepository;
import com.example.DATN_Fashion_Shop_BE.repository.NotificationTranslationRepository;
import com.example.DATN_Fashion_Shop_BE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.DATN_Fashion_Shop_BE.model.TransferStatus.*;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationTranslationRepository notificationTranslationRepository;
    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;

    public Page<NotificationResponse> getUserNotifications(Long userId, String langCode, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findAllByUserId(pageable,userId);
        return notifications.map(
                notification -> NotificationResponse.fromNotification(notification,langCode)
        );
    }

    public TotalNotificationResponse  getNotificationCount(Long userId) {
        Integer count = notificationRepository.countByUserIdAndIsRead(userId, false);
        return new TotalNotificationResponse(count);
    }

    public void markAllNotificationsAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void createNotification(Long userId, String type, String redirectUrl, String imageUrl, List<NotificationTranslationRequest> translations) {
        User user = (userId != null) ? userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")) : null;

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .isRead(false)
                .redirectUrl(redirectUrl)
                .imageUrl(imageUrl)
                .build();

        notification = notificationRepository.save(notification);

        Notification finalNotification = notification;
        List<NotificationTranslations> translationEntities = translations.stream()
                .map(t -> createTranslation(finalNotification, t.getLangCode(), t.getTitle(), t.getMessage()))
                .collect(Collectors.toList());

        notificationTranslationRepository.saveAll(translationEntities);
    }

    private NotificationTranslations createTranslation(Notification notification, String langCode, String title, String message) {
        Language language = languageRepository.findByCode(langCode)
                .orElseThrow(() -> new RuntimeException("Language not found: " + langCode));

        return NotificationTranslations.builder()
                .notification(notification)
                .language(language)
                .title(title)
                .message(message)
                .build();
    }


    /**
     * Thông báo trạng thái Order bằng tiếng Việt
     */
    public String getVietnameseMessage(Long orderId, OrderStatus status) {
        return switch (status.getStatusName()) {
            case "PENDING" -> "Đơn hàng #" + orderId + " đang chờ xác nhận.";
            case "PROCESSING" -> "Đơn hàng #" + orderId + " đang được xử lý.";
            case "SHIPPED" -> "Đơn hàng #" + orderId + " đang được giao.";
            case "DELIVERED" -> "Đơn hàng #" + orderId + " đã giao thành công.";
            case "CANCELLED" -> "Đơn hàng #" + orderId + " đã bị hủy.";
            case "DONE" -> "Đơn hàng #" + orderId + " đã hoàn thành.";
            default -> "Trạng thái đơn hàng không xác định.";
        };
    }

    public String getEnglishMessage(Long orderId, OrderStatus status) {
        return switch (status.getStatusName()) {
            case "PENDING" -> "Order #" + orderId + " is pending confirmation.";
            case "PROCESSING" -> "Order #" + orderId + " is being processed.";
            case "SHIPPED" -> "Order #" + orderId + " is being shipped.";
            case "DELIVERED" -> "Order #" + orderId + " has been delivered successfully.";
            case "CANCELLED" -> "Order #" + orderId + " has been canceled.";
            case "DONE" -> "Order #" + orderId + " has been completed.";
            default -> "Unknown order status.";
        };
    }

    public String getJapaneseMessage(Long orderId, OrderStatus status) {
        return switch (status.getStatusName()) {
            case "PENDING" -> "注文 #" + orderId + " は確認待ちです。";
            case "PROCESSING" -> "注文 #" + orderId + " は処理中です。";
            case "SHIPPED" -> "注文 #" + orderId + " は発送されました。";
            case "DELIVERED" -> "注文 #" + orderId + " が正常に配達されました。";
            case "CANCELLED" -> "注文 #" + orderId + " はキャンセルされました。";
            case "DONE" -> "注文 #" + orderId + " は完了しました。";
            default -> "不明な注文ステータスです。";
        };
    }

}
