package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.service.GHNService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ghn")
@AllArgsConstructor
public class GhnController {

    private final GHNService ghnService;



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
