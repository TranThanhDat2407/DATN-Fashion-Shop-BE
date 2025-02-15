package com.example.DATN_Fashion_Shop_BE.dto.response.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressReponse {
    private Long id;
    private String street;
    private String district;
    private String ward;
    private String province;
    private Double latitude;
    private Double longitude;
}
