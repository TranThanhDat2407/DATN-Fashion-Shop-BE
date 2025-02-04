package com.example.DATN_Fashion_Shop_BE.dto.request.product;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    private String status;
    private Double basePrice;
    private Boolean isActive;
    private List<CreateProductTranslationRequest> translations;
}
