package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.model.HolidayCouponTranslation;
import com.example.DATN_Fashion_Shop_BE.repository.HolidayCouponTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayCouponTranslationService {
    private final HolidayCouponTranslationRepository holidayCouponTranslationRepository;

    public List<CouponTranslationDTO> getTranslationsByType(String couponType) {
        List<HolidayCouponTranslation> translations = holidayCouponTranslationRepository.findByCouponType(couponType);

        return translations.stream()
                .map(t -> new CouponTranslationDTO(t.getName(), t.getDescription(), t.getLanguageCode()))
                .collect(Collectors.toList());
    }
    public Optional<HolidayCouponTranslation> getTranslation(String couponType, String languageCode) {
        return holidayCouponTranslationRepository.findByCouponTypeAndLanguageCode(couponType, languageCode);
    }
}
