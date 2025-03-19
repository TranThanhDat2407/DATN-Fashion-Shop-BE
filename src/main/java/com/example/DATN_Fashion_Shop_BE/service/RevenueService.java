package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top10Products;
import com.example.DATN_Fashion_Shop_BE.repository.OrderDetailRepository;
import com.example.DATN_Fashion_Shop_BE.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@AllArgsConstructor
public class RevenueService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    /**
     * Lấy doanh thu theo ngày
     */
    public BigDecimal getRevenueByDay(LocalDate date) {
        return orderRepository.findTotalRevenueByDay(date).orElse(BigDecimal.ZERO);
    }


    /**
     * Lấy doanh thu theo tháng
     */
    public BigDecimal getRevenueByMonth(int year, int month) {
        return orderRepository.findTotalRevenueByMonth(year, month).orElse(BigDecimal.ZERO);
    }

    /**
     * Lấy doanh thu theo năm
     */
    public BigDecimal getRevenueByYear(int year) {
        return orderRepository.findTotalRevenueByYear(year).orElse(BigDecimal.ZERO);
    }

    /**
     * Lấy danh sách top 10 sản phẩm bán chạy nhất
     */

    public Page<Top10Products> getTopSellingProducts(String languageCode, Pageable pageable) {
        return orderDetailRepository.findTopSellingProducts(languageCode, pageable);
    }

}
