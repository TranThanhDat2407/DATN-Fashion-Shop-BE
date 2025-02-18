package com.example.DATN_Fashion_Shop_BE.dto.request.store;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStoreRequest {
    private String name;
    private String phoneNumber;
    private String email;
    private LocalDateTime openHour;
    private LocalDateTime closeHour;
    private Boolean isActive;

    // Thông tin Address
    private String street;
    private String city;
    private String district;
    private String ward;
    private String full_address;
    private Double latitude;
    private Double longitude;

}
