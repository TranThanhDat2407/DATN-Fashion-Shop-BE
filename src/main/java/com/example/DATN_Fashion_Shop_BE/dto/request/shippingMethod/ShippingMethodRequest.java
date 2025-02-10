package com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod;

import com.example.DATN_Fashion_Shop_BE.dto.request.CarItem.CarItemDTO;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShippingMethodRequest {

    @Builder.Default
    private Integer payment_type_id = 2;

    @Builder.Default
    private String required_note = "KHONGCHOXEMHANG";

    private String to_name;
    private String to_phone;
    private String to_address;
    private String to_ward_name;
    private String to_district_name;
    private String to_province_name;
    private Integer weight;

    @Builder.Default
    private Integer service_type_id = 2;

    private List<CarItemDTO> items;
}

//        (warehouse.getAddress.getProvince())
//        (warehouse.getAddress.getDistrict())
//                (warehouse.getAddress.getWard())
//                (userAddress.getAddress().getCity())
//            (userAddress.getAddress().getDistrict() )
//            (userAddress.getAddress().getWard()
//        (cứng hoặc vd (5 món đồ < 500g, > 1000g))
//            (tổng value/ )
//            (cứng)