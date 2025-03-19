package com.example.DATN_Fashion_Shop_BE.dto.request.product;

import lombok.*;

@Getter
@Setter
public class SetCategoryProductRequest {
    private Long id;
    private Long categoryId;
}
