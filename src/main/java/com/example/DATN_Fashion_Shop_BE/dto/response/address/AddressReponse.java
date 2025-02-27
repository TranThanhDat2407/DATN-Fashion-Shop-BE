package com.example.DATN_Fashion_Shop_BE.dto.response.address;

import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.UserAddress;
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


    public static AddressReponse fromAddress(Address address) {
        return AddressReponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .district(address.getDistrict())
                .ward(address.getWard())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }
}
