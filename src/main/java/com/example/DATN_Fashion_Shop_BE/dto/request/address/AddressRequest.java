package com.example.DATN_Fashion_Shop_BE.dto.request.address;

import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    @NotBlank(message = MessageKeys.ADDRESS_STREET_NOT_BLANK)
    private String street;

    @NotBlank(message = MessageKeys.ADDRESS_DISTRICT_NOT_BLANK)
    private String district;

    @NotBlank(message = MessageKeys.ADDRESS_WARD_NOT_BLANK)
    private String ward;

    @NotBlank(message = MessageKeys.ADDRESS_PROVINCE_NOT_BLANK)
    private String province;

    @NotNull(message = MessageKeys.ADDRESS_LATITUDE_NOT_NULL)
    private Double latitude;

    @NotNull(message = MessageKeys.ADDRESS_LONGITUDE_NOT_NULL)
    private Double longitude;

    @NotBlank(message = MessageKeys.ADDRESS_FIRST_NAME_NOT_BLANK)
    @Size(max = 50, message = MessageKeys.ADDRESS_FIRST_NAME_SIZE)
    private String firstName;

    @NotBlank(message = MessageKeys.ADDRESS_LAST_NAME_NOT_BLANK)
    @Size(max = 50, message = MessageKeys.ADDRESS_LAST_NAME_SIZE)
    private String lastName;

    @NotBlank(message = MessageKeys.ADDRESS_PHONE_NUMBER_NOT_BLANK)
    @Pattern(regexp = "^(0[1-9][0-9]{8,9})$", message = MessageKeys.ADDRESS_PHONE_NUMBER_INVALID)
    private String phoneNumber;
}
