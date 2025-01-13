package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.component.JwtTokenUtil;
import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.ProductDTO;
import com.example.DATN_Fashion_Shop_BE.dto.UpdateUserDTO;
import com.example.DATN_Fashion_Shop_BE.dto.UserDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.UserAdminResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.exception.ExpiredTokenException;
import com.example.DATN_Fashion_Shop_BE.exception.InvalidPasswordException;
import com.example.DATN_Fashion_Shop_BE.exception.PermissionDenyException;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductTranslationRepository productTranslationRepository;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;

    public List<ProductDTO> getProductsWithTranslations(String languageCode,
                                                        Boolean isActive, String searchName, int page, int size) {
        // Lấy sản phẩm theo trạng thái và tên (nếu có tìm kiếm theo tên)
        Pageable pageable = PageRequest.of(page, size);
        List<Product> products = (searchName != null && !searchName.isEmpty())
                ? productRepository.findAllByIsActiveAndName(isActive, searchName, pageable)
                : productRepository.findAllByIsActive(isActive, pageable);

        // Lấy danh sách ID của sản phẩm và bản dịch theo ngôn ngữ
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, String> translationMap = productTranslationRepository
                .findByProductIdInAndLanguageCode(productIds, languageCode).stream()
                .collect(Collectors.toMap(
                        translation -> translation.getProduct().getId(),
                        ProductsTranslation::getName
                ));

        // Bổ sung bản dịch mặc định cho những sản phẩm thiếu bản dịch theo languageCode
        productTranslationRepository.findByProductIdInAndLanguageCode(
                        productIds.stream().filter(id -> !translationMap.containsKey(id)).collect(Collectors.toList()), "en")
                .forEach(translation -> translationMap.putIfAbsent(translation.getProduct().getId(), translation.getName()));

        // Ánh xạ Entity sang DTO
        return products.stream()
                .map(product -> ProductDTO.fromProduct(product, translationMap.getOrDefault(product.getId(), "")))
                .collect(Collectors.toList());
    }
}
