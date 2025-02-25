package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
import com.example.DATN_Fashion_Shop_BE.model.UserAddress;
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
    private String phoneNumber;  // Thêm số điện thoại
    private String firstName;    // Thêm tên người nhận
    private String lastName;
    private Boolean isDefault;

    public static AddressDTO fromAddress(Address address, UserAddress userAddress) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .district(address.getDistrict())
                .ward(address.getWard())
                .province(address.getCity())  // Hoặc address.getProvince() nếu bạn muốn dùng tên tỉnh
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .phoneNumber(userAddress.getPhone())  // Lấy số điện thoại từ UserAddress
                .firstName(userAddress.getFirstName())  // Lấy tên người nhận từ UserAddress
                .lastName(userAddress.getLastName())
                .isDefault(userAddress.getIsDefault())// Lấy họ người nhận từ UserAddress
                .build();
    }
}
