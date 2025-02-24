package com.example.DATN_Fashion_Shop_BE.service;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


@Service
public class VNPayService  {

    private static final String vnp_TmnCode = "IQUTYPIQ";
    private static final String vnp_HashSecret = "HJF2G7EHCHPX0K446LBH17FKQUF56MB5";
    private static final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"; // URL VNPay
    private static final String vnp_ReturnUrl = "http://localhost:4200/client/usd/en/payment_success"; // URL trả về sau khi thanh toán
    private static final String vnp_IpnUrl = "https://c84e-171-251-218-11.ngrok-free.app/api/v1/payment/vnpay_ipn"; // URL VNPay gọi khi thanh toán xong

    public static String hmacSHA512(String data, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append("0");
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo HMAC-SHA512", e);
        }
    }

    public static String createPaymentUrl(long amount, String orderInfo, String txnRef, String ipAddr) {
        SortedMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnp_ReturnUrl);
//        params.put("vnp_IpnUrl", vnp_IpnUrl);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (data.length() > 0) data.append("&");
            data.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        // Tạo chữ ký bảo mật
        String secureHash = hmacSHA512(data.toString(), vnp_HashSecret);
        params.put("vnp_SecureHash", secureHash);

        // Tạo URL thanh toán
        StringBuilder paymentUrl = new StringBuilder(vnp_Url + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (paymentUrl.length() > vnp_Url.length() + 1) paymentUrl.append("&");
            paymentUrl.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return paymentUrl.toString();
    }
}


