package com.example.DATN_Fashion_Shop_BE.dto.response.user;
import com.example.DATN_Fashion_Shop_BE.model.Role;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime dateOfBirth;
    private String gender;
    private String phone;
    @JsonProperty("role")
    private Role role;
    @JsonProperty("is_active")
    private boolean active;
    @JsonProperty("google_account_id")
    private String googleAccountId;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .active(user.getIsActive() != null ? user.getIsActive() : false)
                .googleAccountId(user.getGoogleAccountId())
                .role(user.getRole())
                .build();
    }
}
