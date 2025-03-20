package com.example.DATN_Fashion_Shop_BE.dto.response.staff;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreResponse;
import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.Role;
import com.example.DATN_Fashion_Shop_BE.model.Staff;
import com.example.DATN_Fashion_Shop_BE.model.User;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffResponse extends BaseResponse {
   private Long id;
   private String firstName;
   private String lastName;
   private String email;
   private String phone;
   private Boolean isActive;
   private String gender;
   private StoreResponse store;
   private Role role;

   public static StaffResponse fromStaff(Staff staff) {
      User user = staff.getUser();
      return StaffResponse.builder()
              .id(user.getId())
              .firstName(user.getFirstName())
              .lastName(user.getLastName())
              .email(user.getEmail())
              .phone(user.getPhone())
              .isActive(user.getIsActive())
              .gender(user.getGender())
              .store(StoreResponse.fromStore(staff.getStore()))
              .role(user.getRole())
              .build();
   }
}
