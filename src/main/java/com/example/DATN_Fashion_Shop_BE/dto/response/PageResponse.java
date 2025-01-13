package com.example.DATN_Fashion_Shop_BE.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;  // Danh sách dữ liệu
    private int pageNo;       // Số trang hiện tại
    private int pageSize;     // Số phần tử mỗi trang
    private int totalPages;   // Tổng số trang
    private long totalElements; // Tổng số phần tử
    private boolean first;     // Là trang đầu tiên?
    private boolean last;       // Là trang cuối cùng?

    public static <T> PageResponse<T> fromPage(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
