package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.example.DATN_Fashion_Shop_BE.model.Payment;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class VnPayService {
    private static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNPAY_RETURN_URL = "https://your-website.com/payment/vnpay-return";
    private static final String TMN_CODE = "IQUTYPIQ";
    private static final String HASH_SECRET = "HJF2G7EHCHPX0K446LBH17FKQUF56MB5";

    public String createPaymentUrl(Order order, Payment payment) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", TMN_CODE);
        vnp_Params.put("vnp_Amount", String.valueOf((int) (order.getTotalAmount() * 100))); // Chuyển thành đơn vị VNĐ
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", payment.getTransactionCode());
        vnp_Params.put("vnp_OrderInfo", "Thanh toán đơn hàng " + order.getId());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPAY_RETURN_URL);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        String queryUrl = hashAndBuildUrl(vnp_Params);
        return VNPAY_URL + "?" + queryUrl;
    }

    private String hashAndBuildUrl(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                        .append("&");

                hashData.append(fieldName).append("=").append(value).append("&");
            }
        }

        String secureHash = hmacSHA512(HASH_SECRET, hashData.substring(0, hashData.length() - 1));
        query.append("vnp_SecureHash=").append(secureHash);

        return query.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes());
            return DatatypeConverter.printHexBinary(hash).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa HMAC-SHA512", e);
        }
    }
}
