package com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse;

import com.example.DATN_Fashion_Shop_BE.dto.response.address.AddressReponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.CartItemResponse;
import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.model.UserAddress;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressResponse {
    private Long id;
    private AddressReponse address;
    private Boolean isDefault;
    private String firstName;
    private String lastName;
    private String phone;


    public static UserAddressResponse fromUserAddress(UserAddress userAddress) {
        return UserAddressResponse.builder()
                .id(userAddress.getId())
                .address(AddressReponse.fromAddress(userAddress.getAddress()))
                .isDefault(userAddress.getIsDefault())
                .firstName(userAddress.getUser().getFirstName())
                .lastName(userAddress.getUser().getLastName())
                .phone(userAddress.getUser().getPhone())
                .build();
    }

}
