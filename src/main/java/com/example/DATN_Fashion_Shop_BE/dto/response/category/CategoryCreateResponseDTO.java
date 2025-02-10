package com.example.DATN_Fashion_Shop_BE.dto.response.category;

import com.example.DATN_Fashion_Shop_BE.dto.request.category.CategoryCreateRequestDTO;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateResponseDTO {
    private Long id;
    private CategoryCreateRequestDTO requestCategory;
}
