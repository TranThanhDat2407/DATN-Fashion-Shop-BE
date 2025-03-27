package com.example.DATN_Fashion_Shop_BE.dto.request.email;

import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO {
    private String to;
    private List<OrderDetailResponse> orderDetails;
}
