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
}
