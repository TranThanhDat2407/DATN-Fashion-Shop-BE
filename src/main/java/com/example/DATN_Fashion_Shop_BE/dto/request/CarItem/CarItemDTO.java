package com.example.DATN_Fashion_Shop_BE.dto.request.CarItem;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarItemDTO {
    private String name;
    private int quantity;


}
