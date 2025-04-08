package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.response.order.OrderResponseMail;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductVariantResponse;
import com.example.DATN_Fashion_Shop_BE.model.HolidayCouponTranslation;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreOrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreOrderResponse;
import com.example.DATN_Fashion_Shop_BE.model.OrderDetail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final QRCodeService qrCodeService;
    static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final HolidayCouponTranslationService holidayCouponTranslationService;
    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true); // `true` để gửi email ở định dạng HTML

        mailSender.send(message);
    }
    public void sendVerificationEmail(String to, String firstName, String verificationUrl) throws MessagingException {
        String subject = "🔐 Email Verification";
        String message = "<html><head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }"
                + ".email-container { max-width: 600px; background: #ffffff; margin: 20px auto; padding: 20px; "
                + "border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
                + ".email-header { text-align: center; font-size: 20px; font-weight: bold; color: #333; margin-bottom: 20px; }"
                + ".email-body { font-size: 16px; color: #555; line-height: 1.5; }"
                + ".button-container { text-align: center; margin-top: 20px; }"
                + ".verify-button { display: inline-block; padding: 12px 24px; font-size: 18px; color: #fff; "
                + "background-color: #28a745; border-radius: 5px; text-decoration: none; font-weight: bold; }"
                + ".email-footer { margin-top: 30px; font-size: 14px; text-align: center; color: #777; }"
                + "</style></head><body>"

                + "<div class='email-container'>"
                + "<div class='email-header'>🔐 Email Verification</div>"
                + "<div class='email-body'>"
                + "<p>Dear <b>" + firstName + "</b>,</p>"
                + "<p>Welcome to our service! To keep your account secure, please verify your email "
                + "by clicking the button below:</p>"

                + "<div class='button-container'>"
                + "<a href='" + verificationUrl + "' class='verify-button'>Verify Email</a>"
                + "</div>"

                + "<p style='text-align: center; margin-top: 15px; font-size: 14px; color: #777;'>"
                + "If you did not request this, please ignore this email.</p>"

                + "</div>"
                + "<div class='email-footer'>"
                + "Best regards,<br/><b>Support Team</b><br/>"
                + "📧 Contact us: support@example.com"
                + "</div>"
                + "</div>"

                + "</body></html>";


        sendEmail(to, subject, message);
    }


    public void sendEmailWithAttachment(String to, String subject, String body, String qrCodePath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        // Thêm mã QR dưới dạng inline attachment (ảnh nhúng)
        File qrCodeFile = new File(qrCodePath);
        if (qrCodeFile.exists()) {
            helper.addInline("qrCode", qrCodeFile);
        } else {
            throw new RuntimeException("QR Code file not found: " + qrCodePath);
        }

        mailSender.send(message);
    }

    public void sendCouponEmail(String to, String couponCode, int daysValid, String couponType, String languageCode) {
        try {
            // Lấy bản dịch từ HolidayCouponTranslation
            Optional<HolidayCouponTranslation> translationOpt = holidayCouponTranslationService.getTranslation(couponType, languageCode);

            String title = "🎉 Chúc mừng! Bạn vừa nhận được mã giảm giá 🎊";
            String description = "Hãy tận hưởng ưu đãi từ chúng tôi!";
            String imageUrl = "default-image.jpg"; // Ảnh mặc định

            if (translationOpt.isPresent()) {
                HolidayCouponTranslation translation = translationOpt.get();
                title = translation.getName();
                description = translation.getDescription();
                imageUrl = "http://localhost:8080/uploads/images/coupons/" + couponType + ".jpg"; // Ảnh có thể lưu theo couponType
            }

            // Xây dựng nội dung email
            String emailContent = buildEmailContent(couponCode, imageUrl, daysValid, title, description);

            // Gửi email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("🎁 " + title + " - Nhận ngay mã giảm giá từ Fashion Shop!");
            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email", e);
        }
    }


    private String buildEmailContent(String couponCode, String imageUrl, int daysValid, String title, String description) {
        return "<div style='text-align:center;'>"
                + "<h1>🎉 " + title + " 🎊</h1>"
                + "<p>" + description + "</p>"
                + "<p><img src='" + imageUrl + "' alt='Coupon Image' style='width:100%; max-width:400px; border-radius:10px;'/></p>"
                + "<p><b>Mã giảm giá của bạn:</b> <span style='color:red;font-size:22px;'>" + couponCode + "</span></p>"
                + "<p>Mã này có hiệu lực trong <b>" + daysValid + " ngày</b>. Hãy sử dụng ngay!</p>"
                + "<p><i>Trân trọng,<br>Đội ngũ Fashion Shop</i></p>"
                + "</div>";
    }


    @Async
    public void sendOrderConfirmationEmail(String to, List<OrderDetailResponse> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            log.warn("⚠ Không có chi tiết đơn hàng để gửi email.");
            return;
        }

        try {
            // Log thông tin payment method từ tất cả order details
            log.info("===== THÔNG TIN PHƯƠNG THỨC THANH TOÁN =====");
            for (OrderDetailResponse detail : orderDetails) {
                log.info("Order ID: {} - Payment Method: {}", detail.getOrderId(), detail.getPaymentMethod());
            }
            log.info("=======================================");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            OrderDetailResponse firstDetail = orderDetails.get(0); // Lấy thông tin chung từ đơn hàng đầu tiên


            String subject = "Xác nhận đơn hàng #" + firstDetail.getOrderId();
            String orderDetailsHtml = buildOrderDetailsHtml(orderDetails);

            // Xây dựng nội dung email
            StringBuilder body = new StringBuilder();
            body.append("<html><body>");
            body.append("<p>Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi!</p>");
            body.append("<p><strong>Mã đơn hàng:</strong> ").append(firstDetail.getOrderId()).append("</p>");
            body.append("<p><strong>Người nhận:</strong> ").append(firstDetail.getRecipientName()).append("</p>");
            body.append("<p><strong>Số điện thoại:</strong> ").append(firstDetail.getRecipientPhone()).append("</p>");
            body.append("<p><strong>Địa chỉ giao hàng:</strong> ").append(firstDetail.getShippingAddress()).append("</p>");
            body.append("<p><strong>Phương thức thanh toán:</strong> ").append(firstDetail.getPaymentMethod()).append("</p>");
            body.append("<p><strong>Thuế:</strong> ").append(firstDetail.getTax()).append(" VNĐ</p>");
            body.append("<p><strong>Phí vận chuyển:</strong> ").append(firstDetail.getShippingFee()).append(" VNĐ</p>");
            body.append("<p><strong>Tổng tiền:</strong> ").append(firstDetail.getGrandTotal()).append(" VNĐ</p>");
            body.append("<h3>Chi tiết đơn hàng:</h3>");
            body.append(orderDetailsHtml);
            body.append("<p>💖 Chúc bạn có một ngày tuyệt vời!</p>");
            body.append("<p>Trân trọng,</p>");
            body.append("<p><strong>Đội ngũ cửa hàng BrandShop luôn tận tình phục vụ quý khách</strong></p>");
            body.append("</body></html>");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body.toString(), true);

            // Gắn hình ảnh vào email
            for (OrderDetailResponse detail : orderDetails) {
                log.info("📌 Order ID: {}", detail.getOrderId());
                log.info("📌 Recipient Name: {}", detail.getRecipientName());
                log.info("📌 Recipient Phone: {}", detail.getRecipientPhone());
                if (detail.getImageUrl() != null) {
                    File imageFile = new File(Paths.get("uploads/images/products/", detail.getImageUrl()).toString());
                    log.info("📌 Đường dẫn ảnh: {}", imageFile.getAbsolutePath());


                    if (imageFile.exists()) {
                        FileSystemResource image = new FileSystemResource(imageFile);
                        String contentId = "image" + detail.getOrderDetailId();
                        helper.addInline(contentId, image);
                    } else {
                        log.warn("⚠ Hình ảnh không tồn tại: {}", imageFile.getAbsolutePath());
                    }

                }
            }


            mailSender.send(message);
            log.info("📧 Đã gửi email xác nhận đơn hàng đến {}", to);
        } catch (MessagingException e) {
            log.error("❌ Lỗi khi gửi email: {}", e.getMessage());
            e.printStackTrace();
        }
    }




    private String buildOrderDetailsHtml(List<OrderDetailResponse> orderDetails) {
        StringBuilder html = new StringBuilder();
        html.append("<table border='1' cellspacing='0' cellpadding='5' style='border-collapse: collapse; width: 100%;'>");
        html.append("<tr style='background-color: #f2f2f2; text-align: left;'>");
        html.append("<th>Hình ảnh</th><th>Sản phẩm</th><th>Số lượng</th><th>Màu</th><th>Size</th><th>Giá</th></tr>");

        for (OrderDetailResponse detail : orderDetails) {
            ProductVariantResponse product = detail.getProductVariant();
            String contentId = "image" + detail.getOrderDetailId();
            html.append("<tr>")
                    .append("<td><img src='cid:").append(contentId).append("' width='100' height='100' style='border-radius: 4px;'/></td>")
                    .append("<td>").append(product.getProductName()).append("</td>")
                    .append("<td>").append(detail.getQuantity()).append("</td>")
                    .append("<td>").append(product.getColorName()).append("</td>")
                    .append("<td>").append(product.getSize()).append("</td>")
                    .append("<td>").append(detail.getTotalPrice()).append("</td>")
                    .append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }

    private String buildOrderDetailsHtml2(List<StoreOrderDetailResponse> orderDetails) {
        StringBuilder html = new StringBuilder();
        html.append("<table border='1' cellspacing='0' cellpadding='5' style='border-collapse: collapse; width: 100%;'>");
        html.append("<tr style='background-color: #f2f2f2; text-align: left;'>");
        html.append("<th>Hình ảnh</th><th>Sản phẩm</th><th>Số lượng</th><th>Giá</th>" +
                "<th>Màu</th><th>Size</th><th>Tổng Cộng</th></tr>");

        for (int i = 0; i < orderDetails.size(); i++) {
            StoreOrderDetailResponse detail = orderDetails.get(i);
            String contentId = "image" + i; // Đặt contentId theo chỉ mục để tránh trùng lặp

            html.append("<tr>")
                    .append("<td><img src='cid:").append(contentId).append("' width='100' height='100' style='border-radius: 4px;'/></td>")
                    .append("<td>").append(detail.getProductName()).append("</td>")
                    .append("<td>").append(detail.getQuantity()).append("</td>")
                    .append("<td>").append(formatCurrency(detail.getUnitPrice())).append("</td>")
                    .append("<td>").append(detail.getColorName()).append("</td>")
                    .append("<td>").append(detail.getSizeName()).append("</td>")
                    .append("<td>").append(formatCurrency(detail.getTotalPrice())).append("</td>")
                    .append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }

    @Async
    public void sendOrderConfirmationEmail(String to, StoreOrderResponse storeOrder) {
        if (storeOrder == null || storeOrder.getOrderDetails().isEmpty()) {
            log.warn("⚠ Không có chi tiết đơn hàng để gửi email.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = "Đơn hàng Click&Collect được đặt thành công ";
            String orderDetailsHtml = buildOrderDetailsHtml2(storeOrder.getOrderDetails());

            // Xây dựng nội dung email
            StringBuilder body = new StringBuilder();
            body.append("<html><body>");
            body.append("<p>Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi!</p>");
            body.append("<p><strong style='color: red; font-weight: bold;'>Khi đơn hàng sẵn sàng, chúng tôi sẽ thông báo cho bạn trong thời gian sớm nhất!</strong></p>");
            body.append("<p><strong>Mã đơn hàng:</strong> ").append(storeOrder.getOrderId()).append("</p>");
            body.append("<p><strong>Người nhận:</strong> ").append(storeOrder.getUser().getFirstName())
                    .append(" ")
                    .append(storeOrder.getUser().getLastName()).append("</p>");
            body.append("<p><strong>Số điện thoại:</strong> ").append(storeOrder.getUser().getPhone()).append("</p>");
            body.append("<p><strong>Địa chỉ cửa hàng:</strong> ").append(storeOrder.getShippingAddress()).append("</p>");
            body.append("<p><strong>Phương thức thanh toán:</strong> ");
            if (storeOrder.getPaymentMethod().getMethodName().equals("Pay-in-store")) {
                body.append("Thanh toán tại cửa hàng");
            } else {
                body.append(storeOrder.getPaymentMethod().getMethodName());
            }
            body.append("</p>");
            body.append("<p><strong>Thuế:</strong> ").append(formatCurrency(storeOrder.getTaxAmount())).append("</p>");
            body.append("<p><strong>Phí vận chuyển:</strong> ").append(formatCurrency(storeOrder.getShippingFee())).append("</p>");
            body.append("<p><strong>Tổng tiền:</strong> ").append(formatCurrency(storeOrder.getTotalPrice())).append("</p>");
            body.append("<h3>Chi tiết đơn hàng:</h3>");
            body.append(orderDetailsHtml);

            body.append("<p>Trân trọng,</p>");
            body.append("<p><strong>Đội ngũ cửa hàng BrandShop luôn tận tình phục vụ quý khách</strong></p>");
            body.append("</body></html>");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body.toString(), true);

            // Gắn hình ảnh vào email
            int index = 0;
            for (StoreOrderDetailResponse detail : storeOrder.getOrderDetails()) {
                if (detail.getProductImage() != null) {
                    File imageFile = new File(Paths.get("uploads/images/products/", detail.getProductImage()).toString());
                    log.info("📌 Đường dẫn ảnh: {}", imageFile.getAbsolutePath());

                    if (imageFile.exists()) {
                        FileSystemResource image = new FileSystemResource(imageFile);
                        String contentId = "image" + index++; // Phải khớp với `cid` trong HTML
                        helper.addInline(contentId, image);
                    } else {
                        log.warn("⚠ Hình ảnh không tồn tại: {}", imageFile.getAbsolutePath());
                    }
                }
            }

            mailSender.send(message);
            log.info("📧 Đã gửi email xác nhận đơn hàng đến {}", to);
        } catch (MessagingException e) {
            log.error("❌ Lỗi khi gửi email: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendOrderReadyForPickupEmail(String to, StoreOrderResponse storeOrder) {
        if (storeOrder == null || storeOrder.getOrderDetails().isEmpty()) {
            log.warn("⚠ Không có đơn hàng hoặc chi tiết đơn hàng để gửi email.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = "Đơn hàng #" + storeOrder.getOrderId() + " đã sẵn sàng nhận!";
            String qrContentId = "qrcode_" + UUID.randomUUID(); // Tạo ID duy nhất cho ảnh QR

            // Tạo QR Code trong bộ nhớ
            BufferedImage qrImage = qrCodeService.generateQRCode(String.valueOf(storeOrder.getOrderId()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] qrBytes = baos.toByteArray();

            // Xây dựng nội dung email
            StringBuilder body = new StringBuilder();
            body.append("<html><body>");
            body.append("<p>Thân gửi <strong>").append(storeOrder.getUser().getFirstName())
                    .append(" ").append(storeOrder.getUser().getLastName()).append("</strong>,</p>");
            body.append("<p>Rất cảm ơn quý khách hàng đã lựa chọn mua sắm tại <strong>BrandShop</strong>.</p>");
            body.append("<p><strong>Đơn đặt hàng của quý khách hiện đã sẵn sàng để nhận.</strong></p>");
            body.append("<p>Vui lòng đến trực tiếp quầy <strong>CLICK & COLLECT</strong> tại cửa hàng <strong>")
                    .append(storeOrder.getShippingAddress()).append("</strong>.</p>");
            body.append("<p>Xuất trình mã QR bên dưới hoặc mã vạch thành viên để nhận hàng.</p>");

            // Hiển thị mã QR trong email
            body.append("<p><img src='cid:").append(qrContentId).append("' width='300' height='300'/></p>");

            // Cộng 7 ngày vào updatedAt
            LocalDate lastPickupDate = storeOrder.getUpdatedAt().plusDays(7).toLocalDate();

            body.append("<p><strong style='color: red;'>Ngày cuối cùng có thể lấy hàng: ")
                    .append(lastPickupDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .append("</strong></p>");
            // Thêm thông tin đơn hàng
            body.append("<p><strong>Mã đơn hàng:</strong> ").append(storeOrder.getOrderId()).append("</p>");
            body.append("<p><strong>Người nhận:</strong> ").append(storeOrder.getUser().getFirstName())
                    .append(" ").append(storeOrder.getUser().getLastName()).append("</p>");
            body.append("<p><strong>Số điện thoại:</strong> ").append(storeOrder.getUser().getPhone()).append("</p>");
            body.append("<p><strong>Địa chỉ cửa hàng:</strong> ").append(storeOrder.getShippingAddress()).append("</p>");
            body.append("<p><strong>Phương thức thanh toán:</strong> ");
            if (storeOrder.getPaymentMethod().getMethodName().equals("Pay-in-store")) {
                body.append("Thanh toán tại cửa hàng");
            } else {
                body.append(storeOrder.getPaymentMethod().getMethodName());
            }
            body.append("</p>");
            body.append("<p><strong>Thuế:</strong> ").append(formatCurrency(storeOrder.getTaxAmount())).append("</p>");
            body.append("<p><strong>Phí vận chuyển:</strong> ").append(formatCurrency(storeOrder.getShippingFee())).append("</p>");
            body.append("<p><strong>Tổng tiền:</strong> ").append(formatCurrency(storeOrder.getTotalPrice())).append("</p>");

            // Chi tiết đơn hàng
            body.append("<h3>Chi tiết đơn hàng:</h3>");
            body.append(buildOrderDetailsHtml2(storeOrder.getOrderDetails()));


            body.append("<p>Cảm ơn quý khách và hẹn gặp lại!</p>");
            body.append("</body></html>");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body.toString(), true);

            // Gắn mã QR vào email
            helper.addInline(qrContentId, new ByteArrayResource(qrBytes), "image/png");

            int index = 0;
            for (StoreOrderDetailResponse detail : storeOrder.getOrderDetails()) {
                if (detail.getProductImage() != null) {
                    File imageFile = new File(Paths.get("uploads/images/products/", detail.getProductImage()).toString());
                    log.info("📌 Đường dẫn ảnh: {}", imageFile.getAbsolutePath());

                    if (imageFile.exists()) {
                        FileSystemResource image = new FileSystemResource(imageFile);
                        String contentId = "image" + index++; // Phải khớp với `cid` trong HTML
                        helper.addInline(contentId, image);
                    } else {
                        log.warn("⚠ Hình ảnh không tồn tại: {}", imageFile.getAbsolutePath());
                    }
                }
            }

            mailSender.send(message);
            log.info("📧 Đã gửi email thông báo đơn hàng sẵn sàng nhận đến {}", to);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendPaymentSuccessEmail(String to, StoreOrderResponse storeOrder) {
        if (storeOrder == null) {
            log.warn("⚠ Không có đơn hàng để gửi email.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = "Cảm ơn bạn đã mua sắm tại BRAND!";

            // Xây dựng nội dung email
            StringBuilder body = new StringBuilder();
            body.append("<html><head><meta charset='UTF-8'></head><body>");
            body.append("<p>Thân gửi <strong>").append(storeOrder.getUser().getFirstName())
                    .append(" ").append(storeOrder.getUser().getLastName()).append("</strong>,</p>");
            body.append("<p>Rất cảm ơn quý khách hàng đã lựa chọn mua sắm tại <strong>Brand</strong>.</p>");
            body.append("<p><strong>Mã đơn hàng:</strong> ").append(storeOrder.getOrderId()).append("</p>");
            body.append("<p><strong>Người nhận:</strong> ").append(storeOrder.getUser().getFirstName())
                    .append(" ").append(storeOrder.getUser().getLastName()).append("</p>");
            body.append("<p><strong>Số điện thoại:</strong> ").append(storeOrder.getUser().getPhone()).append("</p>");
            body.append("<p><strong>Địa chỉ cửa hàng:</strong> ").append(storeOrder.getShippingAddress()).append("</p>");
            body.append("<p><strong>Phương thức thanh toán:</strong> ");
            if (storeOrder.getPaymentMethod().getMethodName().equals("Pay-in-store")) {
                body.append("Thanh toán tại cửa hàng");
            } else {
                body.append(storeOrder.getPaymentMethod().getMethodName());
            }
            body.append("</p>");
            body.append("<p><strong>Thuế:</strong> ").append(formatCurrency(storeOrder.getTaxAmount())).append("</p>");
            body.append("<p><strong>Phí vận chuyển:</strong> ").append(formatCurrency(storeOrder.getShippingFee())).append("</p>");
            body.append("<p><strong>Tổng tiền:</strong> ").append(formatCurrency(storeOrder.getTotalPrice())).append("</p>");


            body.append("<h3>Chi tiết đơn hàng:</h3>");
            body.append(buildOrderDetailsHtml2(storeOrder.getOrderDetails()));

            body.append("<p> Cảm ơn bạn đã tin tưởng BRAND! Chúc bạn một ngày tốt lành!</p>");
            body.append("</body></html>");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body.toString(), true);

            int index = 0;
            for (StoreOrderDetailResponse detail : storeOrder.getOrderDetails()) {
                if (detail.getProductImage() != null) {
                    File imageFile = new File(Paths.get("uploads/images/products/", detail.getProductImage()).toString());
                    log.info("📌 Đường dẫn ảnh: {}", imageFile.getAbsolutePath());

                    if (imageFile.exists()) {
                        FileSystemResource image = new FileSystemResource(imageFile);
                        String contentId = "image" + index++; // Phải khớp với `cid` trong HTML
                        helper.addInline(contentId, image);
                    } else {
                        log.warn("⚠ Hình ảnh không tồn tại: {}", imageFile.getAbsolutePath());
                    }
                }
            }

            mailSender.send(message);
            log.info("📧 Đã gửi email xác nhận thanh toán thành công đến {}", to);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email xác nhận thanh toán: {}", e.getMessage());
            e.printStackTrace();
        }
    }


    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " VNĐ";
    }


}
