package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.controller.VnPayController;
import com.example.DATN_Fashion_Shop_BE.dto.response.vnpay.VnPayResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class VNPayService  {
    public static final Logger log = LoggerFactory.getLogger(VNPayService.class);
    private static final String vnp_TmnCode = "IQUTYPIQ";
    private static final String vnp_HashSecret = "HJF2G7EHCHPX0K446LBH17FKQUF56MB5";
    public static final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"; // URL VNPay
    public static final String vnp_ReturnUrl = "http://localhost:4200/client/vnd/vi/payment_success"; // URL trả về sau khi thanh toán
//    private static final String vnp_IpnUrl = "https://tai.kesug.com/api/v1/payment/vnpay_ipn";


    public static String hmacSHA512( final String data, final String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
//            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA512"));
//            byte[] hash = mac.doFinal(data.getBytes());
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

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

    public static String createPaymentUrl(long amount, String orderInfo, String transactionId, String ipAddr) {
        SortedMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transactionId);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        params.put("vnp_IpAddr", ipAddr);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = new Date();
        params.put("vnp_CreateDate", dateFormat.format(now));


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, 5);
        params.put("vnp_ExpireDate", dateFormat.format(calendar.getTime()));

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
            paymentUrl.append(entry.getKey()).append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return paymentUrl.toString();
    }


    public boolean verifyPayment(Map<String, String> vnpParams){
        if (!vnpParams.containsKey("vnp_SecureHash")) {
            return false;
        }

        // Lấy giá trị chữ ký từ request và loại bỏ nó khỏi danh sách tham số
        String vnpSecureHash = vnpParams.get("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        // Xây dựng chuỗi dữ liệu cần hash
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                hashData.append('&');
            }
        }
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }

        // Tính toán chữ ký
        String calculatedHash = hmacSHA512(hashData.toString(),vnp_HashSecret);

        log.info("🔹 Hash nhận từ VNPay: {}", vnpSecureHash);
        log.info("🔹 Hash tính toán: {}", calculatedHash);
        // So sánh với chữ ký nhận được từ VNPay
        return calculatedHash.equalsIgnoreCase(vnpSecureHash);
    }

}


