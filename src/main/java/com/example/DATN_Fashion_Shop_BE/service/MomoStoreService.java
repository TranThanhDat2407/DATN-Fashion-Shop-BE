package com.example.DATN_Fashion_Shop_BE.service;

import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class MomoStoreService {
    private static final Logger log = LoggerFactory.getLogger(MomoStoreService.class);

    private static final String PARTNER_CODE = "MOMOLRJZ20181206";
    private static final String ACCESS_KEY = "mTCKt9W3eU1m39TW";
    private static final String SECRET_KEY = "SetA5RDnLHvt51AULf51DyauxUo3kDU6";
    private static final String API_URL = "https://test-payment.momo.vn/v2/gateway/api";
    private static final String REQUEST_TYPE = "captureWallet";
    private static final String IPN_URL = "https://e5df-171-251-218-14.ngrok-free.app/api/v1/store/momo/callback";
    private static final String LANG = "vi";

    public Map<String, Object> createPaymentAtStore(long storeId, long amount, String orderInfo, String baseOrderId) {
        try {
            String requestId = generateRequestId();
            String orderId = baseOrderId + "_" + System.currentTimeMillis();

            String RETURN_URL =  "http://localhost:4200/staff/" + storeId + "/momo-store-success";

            String rawSignature = String.format(
                    "accessKey=%s&amount=%d&extraData=&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                    ACCESS_KEY, amount, IPN_URL, orderId, orderInfo, PARTNER_CODE, RETURN_URL, requestId, REQUEST_TYPE
            );

            String signature = hmacSHA256(rawSignature, SECRET_KEY);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("accessKey", ACCESS_KEY);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", RETURN_URL);
            requestBody.put("ipnUrl", IPN_URL);
            requestBody.put("extraData", "");
            requestBody.put("requestType", REQUEST_TYPE);
            requestBody.put("signature", signature);
            requestBody.put("lang", LANG);

            log.info("🔰 MoMo Store Payment Request: {}", requestBody);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(API_URL + "/create", HttpMethod.POST, entity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo thanh toán MoMo tại cửa hàng", e);
            throw new RuntimeException("Lỗi khi tạo yêu cầu thanh toán MoMo tại cửa hàng", e);
        }
    }

    public boolean verifyCallback(Map<String, Object> callbackData) {
        try {
            if (!callbackData.containsKey("signature")) {
                return false;
            }

            String receivedSignature = callbackData.get("signature").toString();
            String orderId = callbackData.get("orderId").toString();
            String requestId = callbackData.get("requestId").toString();
            String amount = callbackData.get("amount").toString();

            // Tạo raw signature để verify
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                    ACCESS_KEY,
                    amount,
                    callbackData.get("extraData").toString(),
                    callbackData.get("message").toString(),
                    orderId,
                    callbackData.get("orderInfo").toString(),
                    callbackData.get("orderType").toString(),
                    PARTNER_CODE,
                    callbackData.get("payType").toString(),
                    requestId,
                    callbackData.get("responseTime").toString(),
                    callbackData.get("resultCode").toString(),
                    callbackData.get("transId").toString()
            );


            String calculatedSignature = hmacSHA256(rawSignature, SECRET_KEY);

            log.info("🔹 Signature nhận từ MoMo: {}", receivedSignature);
            log.info("🔹 Signature tính toán: {}", calculatedSignature);

            return calculatedSignature.equals(receivedSignature);
        } catch (Exception e) {
            log.error("Lỗi khi xác minh callback MoMo", e);
            return false;
        }
    }

    public static String hmacSHA256(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public String generateRequestId() {
        return PARTNER_CODE + System.currentTimeMillis();
    }
}
