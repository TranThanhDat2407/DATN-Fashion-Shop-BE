package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    // Lấy promotion đang active
    public Promotion getActivePromotion() throws DataNotFoundException {
        return promotionRepository.findByIsActiveTrue()
                .orElseThrow(() -> new DataNotFoundException("No active promotion found"));
    }

    // Lấy danh sách promotion đang active trong khoảng thời gian cho trước
    public Page<Promotion> getActivePromotionsWithinDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return promotionRepository.findByDateRange(startDate, endDate, pageable);
    }

}
