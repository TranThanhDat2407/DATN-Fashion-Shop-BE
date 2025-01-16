package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.BannerDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.BannerCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.BannerAdminResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.BannerEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
import com.example.DATN_Fashion_Shop_BE.model.Language;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
