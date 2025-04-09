package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.PaypalConfig;
import com.example.DATN_Fashion_Shop_BE.model.Payment;
import jakarta.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaypalService {

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
        String accessToken = getAccessToken();
        String url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + token + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Map.class
        );

        return response.getBody();
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

}

