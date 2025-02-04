package com.example.DATN_Fashion_Shop_BE.dto.response.category;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryTranslationDTO;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEditResponseDTO {
    private Long id;
    private String imageUrl;
    private Long parentId;
    private Boolean isActive;
    private List<CategoryTranslationDTO> translations;
}
