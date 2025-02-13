package com.example.DATN_Fashion_Shop_BE.dto.response.Ghn;

import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.Item;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GhnCreateOrderResponse {
    private String code;
    private String message;
    private GhnOrderData data;
}
