package com.example.DATN_Fashion_Shop_BE.dto.request.product;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductMediaRequest {
    private Integer sortOrder;
    private Integer modelHeight;
    private Long colorValueId; // Cập nhật màu sắc liên quan
    private List<Long> productVariantIds; // Danh sách biến thể sản phẩm muốn liên kết
}
