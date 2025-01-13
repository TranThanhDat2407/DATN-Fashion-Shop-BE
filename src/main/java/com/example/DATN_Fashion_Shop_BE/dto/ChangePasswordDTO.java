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
public class ChangePasswordDTO {
    @NotBlank(message = MessageKeys.PASSWORD_REQUIRED)
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6,}$",
            message = MessageKeys.PASSWORD_INVALID_FORMAT
    )
    private String currentPassword;

    @NotBlank(message = MessageKeys.PASSWORD_REQUIRED)
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6,}$",
            message = MessageKeys.PASSWORD_INVALID_FORMAT
    )
    private String newPassword;

    @NotBlank(message = MessageKeys.PASSWORD_REQUIRED)
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6,}$",
            message = MessageKeys.PASSWORD_INVALID_FORMAT
    )
    private String retypePassword;


    @AssertTrue(message = MessageKeys.PASSWORD_NOT_MATCH)
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(retypePassword);
    }
}
