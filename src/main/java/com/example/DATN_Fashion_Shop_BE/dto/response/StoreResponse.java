package com.example.DATN_Fashion_Shop_BE.dto.response;
import com.example.DATN_Fashion_Shop_BE.model.Role;
import com.example.DATN_Fashion_Shop_BE.model.Store;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    @JsonProperty("is_active")
    private Boolean isActive;
    @JsonProperty("open_hour")
    private LocalDateTime openHour;
    @JsonProperty("close_hour")
    private LocalDateTime closeHour;

    public static StoreResponse fromStore(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .email(store.getEmail())
                .phone(store.getPhone())
                .isActive(store.getIsActive())
                .openHour(store.getOpenHour())
                .closeHour(store.getCloseHour())
                .build();
    }
}
