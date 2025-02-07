package com.example.DATN_Fashion_Shop_BE.dto.response.Ghn;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GhnPreviewResponse {
    private int code;
    private String message;
    private GhnPreviewData data;
}
