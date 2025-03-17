package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.request.Notification.NotificationTranslationRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.notification.NotificationResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.notification.TotalNotificationResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.DATN_Fashion_Shop_BE.model.TransferStatus.*;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationTranslationRepository notificationTranslationRepository;
    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

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


    public List<NotificationTranslationRequest> createCouponTranslations(Coupon coupon) {
        String discountDetails = getDiscountDetails(coupon);

        return List.of(
                new NotificationTranslationRequest("vi", "Ưu đãi đặc biệt!",
                        "Bạn có một mã giảm giá với giá trị là ##" + discountDetails + "##" +
                                " cho đơn hàng có giá trị từ ##" + coupon.getMinOrderValue() + "## trở lên." +
                                " Hạn sử dụng: " + formatDate(coupon.getExpirationDate(), "vi") + "."
                ),
                new NotificationTranslationRequest("en", "Special Offer!",
                        "You have a discount code worth ##" + discountDetails + "##" +
                                " for orders from ##" + coupon.getMinOrderValue() + "## or more." +
                                " Expiry date: " + formatDate(coupon.getExpirationDate(), "en") + "."
                ),
                new NotificationTranslationRequest("jp", "特別オファー！",
                        "あなたには ##" + discountDetails + "## の割引コードがあります。" +
                                " 最低注文額: ##" + coupon.getMinOrderValue() + "##。" +
                                " 有効期限: " + formatDate(coupon.getExpirationDate(), "jp") + "。"
                )
        );
    }

    public String getDiscountDetails(Coupon coupon) {
        if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
            return coupon.getDiscountValue() + "%";
        } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
            return String.valueOf(coupon.getDiscountValue()); // Trả về số tiền gốc, frontend sẽ xử lý format tiền tệ
        }
        return "";
    }

    public String formatDate(LocalDateTime date, String langCode) {
        DateTimeFormatter formatter;
        switch (langCode) {
            case "vi": formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); break;
            case "en": formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm"); break;
            case "jp": formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"); break;
            default: formatter = DateTimeFormatter.ISO_DATE_TIME;
        }
        return date.format(formatter);
    }

    public List<NotificationTranslationRequest> createPromotionTranslations(Promotion promotion) {
        return List.of(
                new NotificationTranslationRequest("vi", "Khuyến mãi hấp dẫn!",
                        "Giảm ngay **##" + promotion.getDiscountPercentage() + "%##** cho các sản phẩm trong chương trình!" +
                                " Chương trình diễn ra từ **" + formatDate(promotion.getStartDate(), "vi") + "** đến **" + formatDate(promotion.getEndDate(), "vi") + "**." +
                                " Đừng bỏ lỡ!"
                ),
                new NotificationTranslationRequest("en", "Exciting Promotion!",
                        "Get **##" + promotion.getDiscountPercentage() + "%##** off on selected products!" +
                                " The promotion runs from **" + formatDate(promotion.getStartDate(), "en") + "** to **" + formatDate(promotion.getEndDate(), "en") + "**." +
                                " Don't miss out!"
                ),
                new NotificationTranslationRequest("jp", "お得なプロモーション！",
                        "選ばれた商品の割引 **##" + promotion.getDiscountPercentage() + "%##**！" +
                                " プロモーション期間: **" + formatDate(promotion.getStartDate(), "jp") + "** から **" + formatDate(promotion.getEndDate(), "jp") + "** まで。" +
                                " お見逃しなく！"
                )
        );
    }

    @Transactional
    //@Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 0 1 */2 ?")
    public void scheduledDeleteAllNotifications() {
        notificationRepository.deleteAll();
        System.out.println("🔄 Tất cả thông báo đã được xóa tự động vào ngày 1 của mỗi 2 tháng!");
    }

    @Transactional
    public void deleteById(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new RuntimeException("Notification with ID " + notificationId + " not found");
        }
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }

    public List<NotificationTranslationRequest> createPromotionForStaff(Promotion promotion) {
        // Lấy danh sách sản phẩm thuộc promotion
        List<Product> products = productRepository.findByPromotion(promotion);

        // Hàm lấy danh sách sản phẩm theo ngôn ngữ
        Function<String, String> getProductNamesByLang = (langCode) -> products.stream()
                .map(product -> {
                    ProductsTranslation translation = product.getTranslationByLanguage(langCode);
                    return  translation.getName();
                })
                .filter(Objects::nonNull) // Lọc ra các sản phẩm có tên hợp lệ
                .collect(Collectors.joining(", "));

        return List.of(
                new NotificationTranslationRequest("vi", "Sản phẩm sắp giảm giá!",
                        "Các mặt hàng sau đây sẽ được giảm **##" + promotion.getDiscountPercentage() + "%##** từ **"
                                + formatDate(promotion.getStartDate(), "vi") + "** đến **" + formatDate(promotion.getEndDate(), "vi")
                                + "**: " + getProductNamesByLang.apply("vi") + ". Đừng bỏ lỡ!"),

                new NotificationTranslationRequest("en", "Upcoming Discount on Products!",
                        "The following products will have a **##" + promotion.getDiscountPercentage() + "%##** discount from **"
                                + formatDate(promotion.getStartDate(), "en") + "** to **" + formatDate(promotion.getEndDate(), "en")
                                + "**: " + getProductNamesByLang.apply("en") + ". Don't miss out!"),

                new NotificationTranslationRequest("jp", "まもなく割引開始！",
                        "次の商品の割引が開始されます！ **##" + promotion.getDiscountPercentage() + "%##** の割引期間: **"
                                + formatDate(promotion.getStartDate(), "jp") + "** から **" + formatDate(promotion.getEndDate(), "jp")
                                + "** まで。対象商品: " + getProductNamesByLang.apply("jp") + "。 お見逃しなく！")
        );
    }

}
