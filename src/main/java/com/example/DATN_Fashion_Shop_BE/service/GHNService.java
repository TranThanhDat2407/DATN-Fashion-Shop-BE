package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.GHNConfig;
import com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod.ShippingMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingOrderReviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class GHNService {
    private static final Logger log = LoggerFactory.getLogger(GHNService.class);
    private final RestTemplate restTemplate;
    private final GHNConfig ghnConfig;
    private static final String BASE_URL = "https://online-gateway.ghn.vn/shiip/public-api/master-data/";
    private static final String TOKEN = "6b3b4d35-e5f0-11ef-b2e4-6ec7c647cc27";


    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Token", TOKEN);
        return headers;
    }

    /**
     * Lấy danh sách tỉnh/thành phố từ GHN
     */
    public ResponseEntity<Map> getProvinces() {
        String url = BASE_URL + "province";
        HttpEntity<String> requestEntity = new HttpEntity<>(createHeaders());

        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
    }

    /**
     * Lấy danh sách quận/huyện dựa vào ProvinceID
     */
    public ResponseEntity<Map> getDistricts(int provinceId) {
        String url = BASE_URL + "district";
        HttpHeaders headers = createHeaders();
        Map<String, Integer> body = new HashMap<>();
        body.put("province_id", provinceId);

        HttpEntity<Map<String, Integer>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
    }

    /**
     * Lấy danh sách phường/xã dựa vào DistrictID
     */
    public ResponseEntity<Map> getWards(int districtId) {
        String url = BASE_URL + "ward";
        HttpHeaders headers = createHeaders();
        Map<String, Integer> body = new HashMap<>();
        body.put("district_id", districtId);

        HttpEntity<Map<String, Integer>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
    }



    public Double getShippingFee(ShippingMethodRequest shippingRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnConfig.getToken());
        headers.set("Content-Type", "application/json");

        HttpEntity<ShippingMethodRequest> requestEntity = new HttpEntity<>(shippingRequest, headers);

        try {
            ResponseEntity<ShippingOrderReviewResponse> response = restTemplate.exchange(
                    "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee",
                    HttpMethod.POST,
                    requestEntity,
                    ShippingOrderReviewResponse.class
            );

            if (response.getBody() != null && response.getBody().getFee() != null) {
                return (double) response.getBody().getFee().getMain_service();
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy phí vận chuyển từ GHN: {}", e.getMessage());
        }

        return 0.0; // Nếu có lỗi, trả về 0.0 thay vì ném lỗi
    }


    public double calculateShippingFee(Address address, List<CartItem> cartItems) {
        String ghnApiUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
        String token = "6b3b4d35-e5f0-11ef-b2e4-6ec7c647cc27";
        String shopId = "195952";

        // Tổng trọng lượng của đơn hàng (giả sử mỗi sản phẩm 200g)
        int totalWeight = cartItems.stream()
                .mapToInt(item -> item.getQuantity() * 200) // 200g mỗi sản phẩm (có thể thay đổi)
                .sum();

        // Chuẩn bị dữ liệu gửi đến GHN
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from_district_id", 195952);
        requestBody.put("service_id", 2);
        requestBody.put("to_district_id", getGhnDistrictId(address.getDistrict())); // Chuyển quận của người nhận sang ID GHN
        requestBody.put("to_ward_code", getGhnWardCode(address.getWard())); // Mã phường GHN
        requestBody.put("height", 10);
        requestBody.put("length", 20);
        requestBody.put("width", 10);
        requestBody.put("weight", totalWeight); // Tổng khối lượng tính theo gram
        requestBody.put("insurance_value", 1000000);

        // Gửi request
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Token", token);
            headers.set("ShopId", shopId);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(
                    ghnApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // Kiểm tra phản hồi từ GHN
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    return ((Number) data.get("total")).doubleValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public Integer getGhnDistrictId(String districtName) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district";
        String token = "6b3b4d35-e5f0-11ef-b2e4-6ec7c647cc27";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Token", token);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Map<String, Object>> districts = (List<Map<String, Object>>) response.getBody().get("data");

                for (Map<String, Object> district : districts) {
                    if (district.get("DistrictName").toString().equalsIgnoreCase(districtName)) {
                        return (Integer) district.get("DistrictID");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Không tìm thấy
    }


    public String getGhnWardCode(String wardName, int districtId) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward";
        String token = "YOUR_GHN_TOKEN"; // Thay bằng Token của bạn

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Token", token);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("district_id", districtId);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("data")) {
                    List<Map<String, Object>> wards = (List<Map<String, Object>>) responseBody.get("data");

                    for (Map<String, Object> ward : wards) {
                        if (ward.get("WardName").toString().equalsIgnoreCase(wardName)) {
                            return ward.get("WardCode").toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // Trả về null nếu không tìm thấy
    }

}