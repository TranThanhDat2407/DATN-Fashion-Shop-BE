package com.example.DATN_Fashion_Shop_BE.dto.request.Ghn;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GhnCreateOrderRequest {
    @NotNull
    private Integer payment_type_id;
    //    private String note;
    @NotNull
    private String required_note;
    //    private String return_phone;
//    private String return_address;
//    private Integer return_district_id;
//    private String return_ward_code;
//    private String client_order_code;
    @NotNull
    private String from_name;
    @NotNull
    private String from_phone;
    @NotNull
    private String from_address;
    @NotNull
    private String from_ward_name;
    @NotNull
    private String from_district_name;
    @NotNull
    private String from_province_name;
    @NotNull
    private String to_name;
    @NotNull
    private String to_phone;
    @NotNull
    private String to_address;
    @NotNull
    private String to_ward_name;
    @NotNull
    private String to_district_name;
    @NotNull
    private String to_province_name;
    @NotNull
    private Integer cod_amount;
    //    private String content;
    @NotNull
    private Integer length;
    @NotNull
    private Integer width;
    @NotNull
    private Integer height;
    @NotNull
    private Integer weight;
    //    private Integer cod_failed_amount;
//    private Integer pick_station_id;
//    private Integer deliver_station_id;
//    private Integer insurance_value;
    @NotNull
    private Integer service_type_id;
    //    private String coupon;
//    private Integer pickup_time;
//    private List<Integer> pick_shift;
    private List<Item> items;
}
