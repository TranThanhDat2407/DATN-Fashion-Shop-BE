package com.example.DATN_Fashion_Shop_BE.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true); // `true` để gửi email ở định dạng HTML

        mailSender.send(message);
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

    public void sendCouponEmail(String to, String couponCode, String imageUrl, int daysValid, String messageType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("🎁 Nhận ngay mã giảm giá từ Fashion Shop!");
            helper.setText(buildEmailContent(couponCode, imageUrl, daysValid, messageType), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email", e);
        }
    }

    private String buildEmailContent(String couponCode, String imageUrl, int daysValid, String messageType) {
        String title = "🎉 Chúc mừng! Bạn vừa nhận được mã giảm giá 🎊";
        String greeting = "Chúng tôi có một món quà dành cho bạn!";
        String specialNote = "";

        switch (messageType) {
            case "BIRTHDAY":
                title = "🎂 Chúc mừng sinh nhật! 🎁";
                greeting = "Sinh nhật bạn thật đặc biệt! Hãy tận hưởng món quà này!";
                specialNote = "<p>🎊 Chúng tôi chúc bạn một ngày tuyệt vời!</p>";
                break;
            case "HOLIDAY":
                title = "🎊 Mừng ngày lễ! Nhận ngay mã giảm giá 🎁";
                greeting = "Hãy tận hưởng ưu đãi đặc biệt nhân dịp lễ!";
                specialNote = "<p>💖 Chúc bạn có một kỳ nghỉ tuyệt vời!</p>";
                break;
            case "WOMEN_DAY":
                title = "💖 Ngày của bạn! Nhận ngay mã giảm giá đặc biệt 🌸";
                greeting = "Cảm ơn bạn đã luôn tuyệt vời! Đây là món quà dành cho bạn!";
                specialNote = "<p>🌷 Chúc bạn một ngày thật vui vẻ và ý nghĩa!</p>";
                break;
        }

        return "<div style='text-align:center;'>"
                + "<h1>" + title + "</h1>"
                + "<p>" + greeting + "</p>"
                + "<p><img src='http://localhost:8080/uploads/images/coupons/" + imageUrl + "' alt='Coupon Image' style='width:100%; max-width:400px; border-radius:10px;'/></p>"
                + "<p><b>Mã giảm giá của bạn:</b> <span style='color:red;font-size:22px;'>" + couponCode + "</span></p>"
                + "<p>Mã này có hiệu lực trong <b>" + daysValid + " ngày</b>. Hãy sử dụng ngay!</p>"
                + specialNote
                + "<p><i>Trân trọng,<br>Đội ngũ Fashion Shop</i></p>"
                + "</div>";
    }


}
