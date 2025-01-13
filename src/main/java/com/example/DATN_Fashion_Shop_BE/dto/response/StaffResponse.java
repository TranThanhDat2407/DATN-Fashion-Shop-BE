package com.example.DATN_Fashion_Shop_BE.dto.response;
import com.example.DATN_Fashion_Shop_BE.model.Role;
import com.example.DATN_Fashion_Shop_BE.model.Staff;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaffResponse {
    private Long id;
    private StoreResponse store;
    private UserResponse user;

    public static StaffResponse fromStaffAndUser(Staff staff, User user) {
        return StaffResponse.builder()
                .id(staff.getId())
                .store(StoreResponse.fromStore(staff.getStore()))
                .user(UserResponse.fromUser(user))
                .build();
    }
}
