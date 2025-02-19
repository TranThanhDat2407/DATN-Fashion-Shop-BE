package com.example.DATN_Fashion_Shop_BE.dto.response.category;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryAdminResponseDTO{
    private Long id;
    private String name;
    private String imageUrl;
    private Boolean isActive;
    private Long parentId;
    private String parentName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
