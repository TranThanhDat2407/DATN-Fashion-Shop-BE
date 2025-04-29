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
public class PreviewOrderRequest {
    @NotNull
    private Integer payment_type_id;
    @NotNull
    private String required_note;
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
    @NotNull
    private Integer length;
    @NotNull
    private Integer width;
    @NotNull
    private Integer height;
    @NotNull
    private Integer weight;
    @NotNull
    private Integer service_type_id;

    private List<Item> items;
}
