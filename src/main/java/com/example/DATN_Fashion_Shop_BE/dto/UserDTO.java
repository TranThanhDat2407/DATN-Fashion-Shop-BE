package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
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

    private LocalDateTime dateOfBirth;

    private String gender;

    @JsonProperty("password")
    @NotBlank(message = MessageKeys.PASSWORD_REQUIRED)
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6,}$",
            message = MessageKeys.PASSWORD_INVALID_FORMAT
    )
    private String password;

    @JsonProperty("retype_password")
    @NotBlank(message = MessageKeys.RETYPE_PASSWORD_REQUIRED)
    private String retypePassword;

    @JsonProperty("google_account_id")
    private String googleAccountId;

    @NotNull(message = MessageKeys.ROLE_ID_REQUIRED)
    @JsonProperty("role_id")
    private Long roleId;

    private Boolean isActive;

    private Long storeId; // Chỉ bắt buộc nếu là Staff hoặc Store Manager

    @AssertTrue(message = MessageKeys.PASSWORD_NOT_MATCH)
    public boolean isPasswordMatching() {
        return password != null && password.equals(retypePassword);
    }
}
