package com.example.DATN_Fashion_Shop_BE.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void sendBirthdayCoupon(String to, String couponCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🎉 Chúc mừng sinh nhật! Nhận ngay mã giảm giá 🎂");
            helper.setText(buildEmailContent(couponCode), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email", e);
        }
    }

    private String buildEmailContent(String couponCode) {
        return "<h1>🎉 Chúc mừng sinh nhật! 🎂</h1>" +
                "<p>Chúng tôi có một món quà dành cho bạn: một mã giảm giá đặc biệt!</p>" +
                "<p><b>Mã giảm giá của bạn:</b> <span style='color:red;font-size:20px;'>" + couponCode + "</span></p>" +
                "<p>Mã này có hiệu lực trong 7 ngày. Hãy tận hưởng!</p>" +
                "<p><i>Trân trọng,<br>Đội ngũ Fashion Shop</i></p>";
    }


}
