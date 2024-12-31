package com.example.DATN_Fashion_Shop_BE.dto.request;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryTranslationDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryAdminResponseDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Boolean isActive;
    private Long parentId;
    private String parentName;
    private LocalDateTime createdAt;  // Thêm createdAt
    private LocalDateTime updatedAt;  // Thêm updatedAt
}
