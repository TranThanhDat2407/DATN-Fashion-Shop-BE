package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.PaypalConfig;
import com.example.DATN_Fashion_Shop_BE.controller.OrderController;
import com.example.DATN_Fashion_Shop_BE.model.Payment;
import jakarta.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaypalService {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final PaypalConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    private String getAccessToken() {
        String auth = config.getClient().getId() + ":" + config.getClient().getSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(config.getClient().getId(), config.getClient().getSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api-m.sandbox.paypal.com/v1/oauth2/token",
                HttpMethod.POST,
                request,
                Map.class
        );

        return response.getBody().get("access_token").toString();
    }

    public String createOrder(Double total, String returnUrl, String cancelUrl) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(
                        Map.of("amount", Map.of("currency_code", "USD", "value", total))
                ),
                "application_context", Map.of(
                        "return_url", returnUrl,
                        "cancel_url", cancelUrl
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api-m.sandbox.paypal.com/v2/checkout/orders",
                HttpMethod.POST,
                request,
                Map.class
        );

        // Lấy URL để redirect người dùng đến PayPal
        List<Map<String, String>> links = (List<Map<String, String>>) response.getBody().get("links");
        return links.stream()
                .filter(link -> "approve".equals(link.get("rel")))
                .findFirst()
                .map(link -> link.get("href"))
                .orElseThrow();
    }

    public Map captureOrder(String token) {
        try {
            String accessToken = getAccessToken();
            String url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + token + "/capture";

            // Get order details before capture to check status
            ResponseEntity<Map> orderResponse = restTemplate.exchange(
                    "https://api.sandbox.paypal.com/v2/checkout/orders/" + token,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders(accessToken)),  // Thêm headers
                    Map.class
            );

            Map orderDetails = orderResponse.getBody();
            String orderStatus = (String) orderDetails.get("status");

            if ("COMPLETED".equals(orderStatus)) {
                log.info("Order already completed. No need to capture again.");
                return orderDetails;  // Trả về thông tin của đơn hàng đã hoàn tất
            }

            // Tiến hành capture nếu đơn hàng chưa hoàn tất
            HttpEntity<Void> captureRequest = new HttpEntity<>(getHeaders(accessToken));
            ResponseEntity<Map> captureResponse = restTemplate.exchange(url, HttpMethod.POST, captureRequest, Map.class);

            log.info("📡 Capture response from PayPal: {}", captureResponse.getBody());
            return captureResponse.getBody();

        } catch (Exception e) {
            log.error("❌ Error while calling PayPal: {}", e.getMessage());
            throw new RuntimeException("Capture failed", e);
        }
    }





    public Map getOrderStatus(String token) {
        String accessToken = getAccessToken();

        String url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + token + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class
        );

        return response.getBody();
    }

    private HttpHeaders getHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // Thêm Authorization Header
        headers.setContentType(MediaType.APPLICATION_JSON); // Đảm bảo gửi Content-Type là JSON
        return headers;
    }


}

