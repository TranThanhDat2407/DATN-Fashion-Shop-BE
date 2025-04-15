package com.example.DATN_Fashion_Shop_BE.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class MomoService {
    private static final Logger log = LoggerFactory.getLogger(MomoService.class);


    private static final String PARTNER_CODE = "MOMO";
    private static final String ACCESS_KEY = "F8BBA842ECF85";
    private static final String SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private static final String API_URL = "https://test-payment.momo.vn/v2/gateway/api";
    public static final String RETURN_URL = "http://localhost:4200/client/vnd/vi/momo-success";
    public static final String IPN_URL = "https://e5df-171-251-218-14.ngrok-free.app/api/v1/momo/callback";
    private static final String REQUEST_TYPE = "captureWallet";
    private static final String LANG = "vi";

    /**
     * Tạo URL thanh toán MoMo
     * @param amount Số tiền thanh toán
     * @param orderInfo Thông tin đơn hàng
     * @param baseOrderId Mã đơn hàng
     * @return Map chứa payUrl và các thông tin khác
     */
    public Map<String, Object> createPayment(long amount, String orderInfo, String baseOrderId) {
        try {
            String requestId = generateRequestId();
            String orderId = baseOrderId + "_" + System.currentTimeMillis();
            // Tạo raw signature
            String rawSignature = String.format(
                    "accessKey=%s&amount=%d&extraData=&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                    ACCESS_KEY, amount, IPN_URL, orderId, orderInfo, PARTNER_CODE, RETURN_URL, requestId, REQUEST_TYPE
            );

            // Tạo signature HMAC-SHA256
            String signature = hmacSHA256(rawSignature, SECRET_KEY);

            // Tạo request body
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

            log.info("MoMo Payment Request: {}", requestBody);

            // **Gửi request đến MoMo**
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(API_URL + "/create", HttpMethod.POST, entity, Map.class);



            return response.getBody();
        } catch (Exception e) {
            log.error("Lỗi khi tạo yêu cầu thanh toán MoMo", e);
            throw new RuntimeException("Lỗi khi tạo yêu cầu thanh toán MoMo", e);
        }
    }

    /**
     * Xác minh callback từ MoMo
     * @param callbackData Dữ liệu callback nhận được
     * @return boolean - true nếu hợp lệ, false nếu không hợp lệ
     */
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

    /**
     * Truy vấn trạng thái thanh toán từ MoMo
     * @param orderId Mã đơn hàng
     * @param requestId Mã yêu cầu
     * @return Map chứa thông tin trạng thái thanh toán
     */
    public Map<String, Object> queryPaymentStatus(String orderId, String requestId) {
        try {
            // Tạo raw signature
            String rawSignature = String.format(
                    "accessKey=%s&orderId=%s&partnerCode=%s&requestId=%s",
                    ACCESS_KEY, orderId, PARTNER_CODE, requestId
            );

            // Tạo signature
            String signature = hmacSHA256(rawSignature, SECRET_KEY);

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("accessKey", ACCESS_KEY);
            requestBody.put("requestId", requestId);
            requestBody.put("orderId", orderId);
            requestBody.put("signature", signature);
            requestBody.put("lang", LANG);

            log.info("MoMo Query Payment Request: {}", requestBody);

            return requestBody;
        } catch (Exception e) {
            log.error("Lỗi khi truy vấn trạng thái thanh toán MoMo", e);
            throw new RuntimeException("Lỗi khi truy vấn trạng thái thanh toán MoMo", e);
        }
    }

    /**
     * Hàm tạo chữ ký HMAC-SHA256
     * @param data Dữ liệu cần hash
     * @param key Khóa bí mật
     * @return Chuỗi đã hash
     */
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

    /**
     * Tạo requestId ngẫu nhiên
     * @return Chuỗi requestId
     */
    public String generateRequestId() {
        return PARTNER_CODE + System.currentTimeMillis();
    }
}
