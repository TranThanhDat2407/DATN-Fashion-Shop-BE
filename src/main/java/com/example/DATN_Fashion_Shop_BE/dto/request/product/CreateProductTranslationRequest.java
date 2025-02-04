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
public class CreateProductTranslationRequest {
    private String name;
    private String description;
    private String material;
    private String care;
    private String langCode;
}
