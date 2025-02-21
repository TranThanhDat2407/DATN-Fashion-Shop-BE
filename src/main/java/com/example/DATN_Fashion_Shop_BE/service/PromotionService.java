package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.request.promotion.PromotionRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.promotion.PromotionResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    public PromotionResponse getActivePromotion() throws DataNotFoundException {
        Promotion promotion = promotionRepository.findByIsActiveTrue()
                .orElseThrow(() -> new DataNotFoundException("No active promotion found"));

        return PromotionResponse.fromPromotion(promotion);
    }

    // Lấy danh sách promotion đang active trong khoảng thời gian cho trước
    public Page<PromotionResponse> getActivePromotionsWithinDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findByDateRange(startDate, endDate, pageable);

        Page<PromotionResponse> responsePage = promotions.map(PromotionResponse::fromPromotion);

        return responsePage;
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        Promotion promotion = Promotion.builder()
                .descriptions(request.getDescription())
                .discountPercentage(request.getDiscountRate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getStartDate().isBefore(LocalDateTime.now()) || request.getStartDate().isEqual(LocalDateTime.now()))
                .build();

        // Lưu Promotion vào DB
        Promotion savedPromotion = promotionRepository.save(promotion);

        // Thêm Products vào Promotion
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(request.getProductIds());
            for (Product product : products) {
                product.setPromotion(savedPromotion);
                productRepository.save(product);
            }
        }

        return PromotionResponse.fromPromotion(savedPromotion);
    }

    @Transactional
    public PromotionResponse updatePromotion(Long promotionId, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        // Cập nhật các trường khác của Promotion
        promotion.setDescriptions(request.getDescription());
        promotion.setDiscountPercentage(request.getDiscountRate());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setIsActive(request.getStartDate().isBefore(LocalDateTime.now()) || request.getStartDate().isEqual(LocalDateTime.now()));

        // Cập nhật Promotion trong DB
        Promotion savedPromotion = promotionRepository.save(promotion);

        // Thêm hoặc cập nhật Products vào Promotion
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(request.getProductIds());
            for (Product product : products) {
                product.setPromotion(savedPromotion);
                productRepository.save(product);
            }
        }

        return PromotionResponse.fromPromotion(savedPromotion);
    }

    @Transactional
    public void deletePromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        promotionRepository.delete(promotion);
    }


    @Transactional
//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 0 * * ?")  // 00:00
    public void deactivateExpiredPromotions() {
        LocalDateTime now = LocalDateTime.now();

        List<Promotion> promotions = promotionRepository.findByEndDateBeforeAndIsActiveTrue(now);

        promotions.forEach(promotion -> {
            promotion.setIsActive(false);
            promotionRepository.save(promotion);
        });
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")  // Chạy mỗi phút
    public void activatePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotionsToActivate = promotionRepository.findByStartDate(now);

        for (Promotion promo : promotionsToActivate) {
            promo.setIsActive(true);
        }

        promotionRepository.saveAll(promotionsToActivate);
    }

    @Transactional
    public PromotionResponse addProductsToPromotion(Long promotionId, List<Long> productIds) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        List<Product> products = productRepository.findAllById(productIds);

        for (Product product : products) {
            product.setPromotion(promotion);  // Gán Promotion cho Product
            productRepository.save(product);  // Lưu lại Product với Promotion
        }

        return PromotionResponse.fromPromotion(promotion);
    }

    @Transactional
    public void updateProductsPromotion(Long promotionId, List<Long> productIds, boolean activate) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        List<Product> products = productRepository.findAllById(productIds);

        for (Product product : products) {
            if (activate) {
                product.setPromotion(promotion);
            } else {
                product.setPromotion(null); // Gỡ bỏ promotion
            }
        }

        productRepository.saveAll(products);
    }
}
