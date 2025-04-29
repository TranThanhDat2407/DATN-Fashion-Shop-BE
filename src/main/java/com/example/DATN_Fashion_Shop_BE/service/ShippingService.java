package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.GHNConfig;
import com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod.ShippingMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingOrderReviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class ShippingService {


    private final RestTemplate restTemplate;


    public ShippingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ShippingOrderReviewResponse getShippingFee(ShippingMethodRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("ShopId", String.valueOf(195952));
        headers.set("Token", "6b3b4d35-e5f0-11ef-b2e4-6ec7c647cc27");
        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<ShippingMethodRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ShippingOrderReviewResponse> response = restTemplate.exchange(
                "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/preview",
                HttpMethod.POST,
                entity,
                ShippingOrderReviewResponse.class
        );

        return response.getBody();

    }



}
