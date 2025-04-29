package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.ShippingFeeRequest;
import com.example.DATN_Fashion_Shop_BE.service.GHNService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ghn")
@AllArgsConstructor
public class GhnController {
    private static final Logger log = LoggerFactory.getLogger(GhnController.class);
    private final GHNService ghnService;

    @PostMapping("/calculate")
    public ResponseEntity<Double> calculateShippingFee(@RequestBody ShippingFeeRequest request) {

        log.info("📥 Dữ liệu nhận từ frontend: " + request);
        System.out.println("🏠 Province nhận được: " + request.getAddress().getCity());

        double fee = ghnService.calculateShippingFee(request.getAddress(), request.getCartItems());
        return ResponseEntity.ok(fee);
    }

    @GetMapping("/province")
    public ResponseEntity<Map> getProvinces() {
        return ghnService.getProvinces();
    }

    @GetMapping("/district")
    public ResponseEntity<Map> getDistricts(@RequestParam int provinceId) {
        return ghnService.getDistricts(provinceId);
    }

    @GetMapping("/ward")
    public ResponseEntity<Map> getWards(@RequestParam int districtId) {
        return ghnService.getWards(districtId);
    }


}
