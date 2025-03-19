package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top10Products;
import com.example.DATN_Fashion_Shop_BE.service.RevenueService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/revenue")
@AllArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping("/daily")
    public ResponseEntity<BigDecimal> getDailyRevenue(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(revenueService.getRevenueByDay(date));
    }

    @GetMapping("/monthly")
    public ResponseEntity<BigDecimal> getMonthlyRevenue(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(revenueService.getRevenueByMonth(year, month));
    }

    @GetMapping("/yearly")
    public ResponseEntity<BigDecimal> getYearlyRevenue(@RequestParam int year) {
        return ResponseEntity.ok(revenueService.getRevenueByYear(year));
    }

    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<Page<Top10Products>>> getTopSellingProducts(
            @RequestParam String languageCode,
            Pageable pageable) {
        Page<Top10Products> page = revenueService.getTopSellingProducts(languageCode, pageable);

        ApiResponse<Page<Top10Products>> response = ApiResponse.<Page<Top10Products>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Danh sách sản phẩm bán chạy lấy thành công")
                .data(page)
                .errors(Collections.emptyList())
                .build();

        return ResponseEntity.ok(response);
    }
}
