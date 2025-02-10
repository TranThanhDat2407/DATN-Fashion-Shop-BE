package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod.ShippingMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingOrderReviewResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service

public class ShippingService {
//    @Value("${shipping.api.url}")
//   private final String shippingApiUrl = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/preview";
//    @Value("${shipping.api.key}")
//    private final String shippingApiKey = "c22baea9-bd0e-11ef-a89d-dab02cbaab48";

    private static RestTemplate restTemplate;

    public ShippingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static   ShippingOrderReviewResponse getShippingFee(ShippingMethodRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Token", "c22baea9-bd0e-11ef-a89d-dab02cbaab48");

        HttpEntity<ShippingMethodRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ShippingOrderReviewResponse> response = restTemplate.exchange(
                "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create",
                HttpMethod.POST,
                entity,
                ShippingOrderReviewResponse.class
        );

        return response.getBody();
//        System.out.println("GHN Response JSON: " + response.getBody());
    }

}
