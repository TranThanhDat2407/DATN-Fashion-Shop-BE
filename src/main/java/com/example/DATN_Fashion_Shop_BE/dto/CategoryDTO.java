package com.example.DATN_Fashion_Shop_BE.dto;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    private Long id;
    private String imageUrl;
    private String name;
    private Boolean isActive;
}
