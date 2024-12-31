package com.example.DATN_Fashion_Shop_BE.dto.request;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryDTO;
import lombok.*;

import java.util.List;

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
