package com.example.DATN_Fashion_Shop_BE.dto.response.store;
import com.example.DATN_Fashion_Shop_BE.model.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponse {
   private Long id;
   private String name;
   private String email;
   private String phone;
   private Double latitude;
   private Double longitude;
   private Boolean isActive;
   @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
   private LocalDateTime openHour;
   @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
   private LocalDateTime closeHour;
   private String fullAddress;

   public static StoreResponse fromStore(Store store) {
      return StoreResponse.builder()
              .id(store.getId())
              .name(store.getName())
              .email(store.getEmail())
              .phone(store.getPhone())
              .isActive(store.getIsActive())
              .latitude(store.getAddress().getLatitude())
              .longitude(store.getAddress().getLongitude())
              .openHour(store.getOpenHour() != null ? store.getOpenHour() : null)
              .closeHour(store.getCloseHour() != null ? store.getCloseHour() : null)
              .fullAddress(store.getAddress().getFullAddress())
              .build();
   }
}
