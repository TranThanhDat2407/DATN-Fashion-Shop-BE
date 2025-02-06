package com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Fee {
    private Integer main_service;
    private Integer insurance;
    private Integer cod_fee;
    private Integer station_do;
    private Integer station_pu;
    private Integer returnFee;
    private Integer r2s;
    private Integer return_again;
    private Integer coupon;
    private Integer document_return;
    private Integer double_check;
    private Integer double_check_deliver;
    private Integer pick_remote_areas_fee;
    private Integer deliver_remote_areas_fee;
    private Integer pick_remote_areas_fee_return;
    private Integer deliver_remote_areas_fee_return;
    private Integer cod_failed_fee;
}
