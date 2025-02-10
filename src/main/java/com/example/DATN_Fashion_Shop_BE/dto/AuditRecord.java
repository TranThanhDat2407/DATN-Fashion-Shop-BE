package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditRecord<T> {
    private Long revision;
    private T entity;
}
