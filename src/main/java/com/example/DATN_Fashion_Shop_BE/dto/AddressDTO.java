package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
import jakarta.persistence.Column;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long id;
    private String street;
    private String district;
    private String ward;
    private String province;
    private Double latitude;
    private Double longitude;

    public static AddressDTO fromAddress(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .district(address.getDistrict())
                .ward(address.getWard())
                .province(address.getCity())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }
}
