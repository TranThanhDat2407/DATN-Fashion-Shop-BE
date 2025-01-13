package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateUserDTO {
    @JsonProperty("first_name")
    @NotBlank(message = MessageKeys.FIRST_NAME_REQUIRED)
    private String firstName;

    @JsonProperty("last_name")
    @NotBlank(message = MessageKeys.LAST_NAME_REQUIRED)
    private String lastName;

    @JsonProperty("phone")
    @NotBlank(message = MessageKeys.PHONE_REQUIRED)
    @Pattern(
            regexp = "^0(3[2-9]|5[2-9]|7[0|6-9]|8[1-9]|9[0-9])\\d{7}$",
            message = MessageKeys.PHONE_INVALID_FORMAT
    )
    private String phone;

    @JsonProperty("email")
    @NotBlank(message = MessageKeys.EMAIL_REQUIRED)
    @Email(message = MessageKeys.EMAIL_INVALID_FORMAT)
    private String email;

    @JsonProperty("date_of_birth")
    private LocalDateTime dateOfBirth;

    @JsonProperty("gender")
    private String gender; // Có thể là "Male", "Female", "Other"

    @JsonProperty("is_active")
    private Boolean isActive;
}
