package com.example.DATN_Fashion_Shop_BE.dto.response.user;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
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
public class UserAdminResponse extends BaseResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime dateOfBirth;
    @JsonProperty("role")
    private Role role;
    @JsonProperty("isActive")
    private boolean active;

    public static UserAdminResponse fromUser(User user) {
        UserAdminResponse userAdminResponse = UserAdminResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .active(user.getIsActive())
                .role(user.getRole())
                .build();
        userAdminResponse.setCreatedAt(user.getCreatedAt());
        userAdminResponse.setUpdatedAt(user.getUpdatedAt());
        userAdminResponse.setCreatedBy(user.getCreatedBy());
        userAdminResponse.setUpdatedBy(user.getUpdatedBy());
        return userAdminResponse;
    }
}
