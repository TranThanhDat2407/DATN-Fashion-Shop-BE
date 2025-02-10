package com.example.DATN_Fashion_Shop_BE.dto.request.product;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductMediaRequest {

    // Thứ tự sắp xếp (nếu có)
    private Integer sortOrder;

    // Chiều cao mẫu (nếu có)
    private Integer modelHeight;

    // ID của AttributeValue cho màu sắc (nếu có)
    private Long colorValueId;

    private MultipartFile file;
}
