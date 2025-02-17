package com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod;

import com.example.DATN_Fashion_Shop_BE.dto.request.CarItem.CarItemDTO;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShippingMethodRequest {

    @NotNull
    @Builder.Default
    private Integer payment_type_id = 2;
    @NotNull
    @Builder.Default
    private String required_note = "KHONGCHOXEMHANG";

    // Thông tin người gửi
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

    // Thông tin người nhận
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

    // Thông tin kiện hàng
    @NotNull
    private Integer length;
    @NotNull
    private Integer width;
    @NotNull
    private Integer height;
    @NotNull
    private Integer weight;
    @NotNull
    @Builder.Default
    private Integer service_type_id = 2;



    private List<CarItemDTO> items;
}

