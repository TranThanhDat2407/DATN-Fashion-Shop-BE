package com.example.DATN_Fashion_Shop_BE.dto.response.Ghn;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Fee {
    private int main_service;
    private int insurance;
    private int station_do;
    private int station_pu;
    private int returnFee;
    private int r2s;
    private int coupon;
    private int cod_failed_fee;
}
