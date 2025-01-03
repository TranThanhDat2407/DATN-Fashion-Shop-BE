package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.LanguageDTO;
import com.example.DATN_Fashion_Shop_BE.model.Language;
import com.example.DATN_Fashion_Shop_BE.repository.LanguageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LanguageService {

    private LanguageRepository languageRepository;

    private final LocalizationUtils localizationUtils;

    public List<LanguageDTO> getLanguages() {
        // Lấy danh sách tất cả ngôn ngữ từ cơ sở dữ liệu
        List<Language> languages = languageRepository.findAll();

        // Sử dụng hàm từ LanguageDTO để chuyển đổi
        return languages.stream()
                .map(LanguageDTO::fromLanguage)
                .collect(Collectors.toList());
    }


}
