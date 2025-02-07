package com.example.DATN_Fashion_Shop_BE.dto.request.category;

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
public class CategoryCreateRequestDTO {
    private String imageUrl; // URL hình ảnh của category
    private Long parentId; // ID của category cha (có thể null)
    @NotEmpty(message = MessageKeys.INSERT_CATEGORY_EMPTY_TRANS)
    @Valid
    private List<CategoryTranslationDTO> translations; // Danh sách bản dịch
}
