package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.GHNConfig;
import com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod.ShippingMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingOrderReviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GHNService {
    private static final Logger log = LoggerFactory.getLogger(GHNService.class);
    private final RestTemplate restTemplate;
    private final GHNConfig ghnConfig;
//    private static final String BASE_URL = "https://online-gateway.ghn.vn/shiip/public-api/master-data/";
    private static final String BASE_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/";
    private static final String TOKEN = "6b3b4d35-e5f0-11ef-b2e4-6ec7c647cc27";
//    private static final String TOKEN = "885c111e-e5e9-11ef-990e-cecd68e7eb91";
//885c111e-e5e9-11ef-990e-cecd68e7eb91
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



    public double calculateShippingFee(Address address, List<CartItem> cartItems) {
        String ghnApiUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
//        String ghnApiUrl = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
//        https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee
        String token = "6b3b4d35-e5f0-11ef-b2e4-6ec7c647cc27";
//        String token = "885c111e-e5e9-11ef-990e-cecd68e7eb91";
        String shopId = "195952";
//        String shopId = "5622599";
//        5622599


        // Lấy districtId và wardCode từ GHN API
        Integer districtId = getGhnDistrictId(address.getCity(), address.getDistrict());
        if (districtId == null) {
            log.warn("⚠ Không tìm thấy District ID cho: {}", address.getCity());
            return 0.0;
        }
//        String fromWardCode = getGhnWardCode("21705", 1457);
//        if (fromWardCode == null) {
//            log.warn("⚠ Không tìm thấy From Ward Code, dùng giá trị mặc định.");
//            fromWardCode = "21705";
//        }
        String wardCode = getGhnWardCode(address.getWard(), districtId);
        if (wardCode == null) {
            log.warn("⚠ Không tìm thấy Ward Code cho: {} - {}", address.getDistrict(), address.getWard());
            return 0.0;
        }

        log.debug("✅ Lấy thành công District ID: {}, Ward Code: {}", districtId, wardCode);

        int totalWeight = cartItems.stream().mapToInt(item -> item.getQuantity() * 300).sum();
        if (totalWeight == 0) {
            log.warn("⚠ Tổng trọng lượng sản phẩm bằng 0, đặt giá trị mặc định là 300g.");
            totalWeight = 300;
        }

        // Chuẩn bị dữ liệu gửi đến GHN
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("service_type_id", 2);
        requestBody.put("service_id", 53321);
        requestBody.put("from_district_id", 1457);
        requestBody.put("from_ward_code","21705");
        requestBody.put("to_district_id", districtId);
        requestBody.put("to_ward_code", wardCode);
        requestBody.put("height", 20);
        requestBody.put("length", 30);
        requestBody.put("width", 20);
        requestBody.put("weight", totalWeight);
        requestBody.put("insurance_value", 1000000);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Token", token);
            headers.set("ShopId", shopId);

            log.debug("🛠 Request gửi đến GHN: {}", new ObjectMapper().writeValueAsString(requestBody));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(
                    ghnApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );


            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    if (data.containsKey("total")) {
                        return ((Number) data.get("total")).doubleValue();
                    }
                }
                log.info("📩 Phản hồi GHN nhưng không có dữ liệu hợp lệ: {}", response.getBody());
            } else {
                log.error("❌ Lỗi GHN HTTP: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi gọi API GHN: {}", e.getMessage(), e);
        }

        return 0.0;
    }


    public Integer getGhnDistrictId(String provinceName, String districtName) {
        String provinceUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province";
        String districtUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district";
//        String provinceUrl = "https://online-gateway.ghn.vn/shiip/public-api/master-data/province";
//        String districtUrl = "https://online-gateway.ghn.vn/shiip/public-api/master-data/district";
        String token = "6b3b4d35-e5f0-11ef-b2e4-6ec7c647cc27";
//        String token = "885c111e-e5e9-11ef-990e-cecd68e7eb91";
//        https://online-gateway.ghn.vn/shiip/public-api/master-data/province
//        https://online-gateway.ghn.vn/shiip/public-api/master-data/district

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Token", token);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            // Lấy danh sách tỉnh/thành phố
            ResponseEntity<Map> provinceResponse = restTemplate.exchange(provinceUrl, HttpMethod.GET, requestEntity, Map.class);

            if (provinceResponse.getStatusCode() == HttpStatus.OK && provinceResponse.getBody() != null) {
                List<Map<String, Object>> provinces = (List<Map<String, Object>>) provinceResponse.getBody().get("data");

                if (provinces != null) {
                    Optional<Map<String, Object>> matchedProvince = provinces.stream()
                            .filter(province -> provinceName.equalsIgnoreCase(province.get("ProvinceName").toString()))
                            .findFirst();

                    if (matchedProvince.isPresent()) {
                        Integer provinceId = (Integer) matchedProvince.get().get("ProvinceID");
                        log.debug("✅ Tìm thấy Province ID cho {}: {}", provinceName, provinceId);

                        // Lấy danh sách quận/huyện theo provinceId
                        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(districtUrl)
                                .queryParam("province_id", provinceId);

                        ResponseEntity<Map> districtResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, requestEntity, Map.class);

                        if (districtResponse.getStatusCode() == HttpStatus.OK && districtResponse.getBody() != null) {
                            List<Map<String, Object>> districtList = (List<Map<String, Object>>) districtResponse.getBody().get("data");

                            if (districtList != null) {
                                Optional<Map<String, Object>> matchedDistrict = districtList.stream()
                                        .filter(district -> {
                                            String districtMainName = district.get("DistrictName").toString();
                                            List<String> nameExtensions = (List<String>) district.get("NameExtension");

                                            return normalize(districtMainName).equalsIgnoreCase(normalize(districtName)) ||
                                                    nameExtensions.stream().anyMatch(name -> normalize(name).equalsIgnoreCase(normalize(districtName)));
                                        })
                                        .findFirst();

                                if (matchedDistrict.isPresent()) {
                                    Integer districtId = (Integer) matchedDistrict.get().get("DistrictID");
                                    log.debug("✅ Tìm thấy District ID cho {} - {}: {}", provinceName, districtName, districtId);
                                    return districtId;
                                }
                            }
                        }

                    }
                }
            } else {
                log.error("❌ Lỗi khi gọi API GHN lấy Province: HTTP {}", provinceResponse.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Exception khi gọi API GHN: ", e);
        }
        return null;
    }


    public String getGhnWardCode(String wardName, int districtId) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward";
//        String url = "https://online-gateway.ghn.vn/shiip/public-api/master-data/ward";
        String token = "6b3b4d35-e5f0-11ef-b2e4-6ec7c647cc27";
//        String token = "885c111e-e5e9-11ef-990e-cecd68e7eb91";
//        https://online-gateway.ghn.vn/shiip/public-api/master-data/ward?district_id
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Token", token);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("district_id", districtId);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> wards = (List<Map<String, Object>>) response.getBody().get("data");

                if (wards != null) {
                    Optional<Map<String, Object>> matchedWard = wards.stream()
                            .filter(ward -> {
                                String wardMainName = ward.get("WardName").toString();
                                List<String> nameExtensions = (List<String>) ward.get("NameExtension");

                                return normalize(wardMainName).equalsIgnoreCase(normalize(wardName)) ||
                                        nameExtensions.stream().anyMatch(name -> normalize(name).equalsIgnoreCase(normalize(wardName)));
                            })
                            .findFirst();


                    if (matchedWard.isPresent()) {
                        String wardCode = matchedWard.get().get("WardCode").toString();
                        log.debug("✅ Tìm thấy Ward Code cho {} - {}: {}", districtId, wardName, wardCode);
                        return wardCode;
                    }
                }
            }
            log.warn("⚠ Không tìm thấy Ward Code cho {} - {}", districtId, wardName);
        } catch (Exception e) {
            log.error("❌ Exception khi gọi API GHN: ", e);
        }

        return null;
    }


    private String normalize(String input) {
        if (input == null) return "";

        // Bước 1: Chuẩn hóa Unicode để loại bỏ dấu tiếng Việt
        String noDiacritics = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        // Bước 2: Chuyển về chữ thường và loại bỏ ký tự đặc biệt, nhưng giữ lại dấu cách
        return noDiacritics.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim().replaceAll("\\s+", " ");
    }


}