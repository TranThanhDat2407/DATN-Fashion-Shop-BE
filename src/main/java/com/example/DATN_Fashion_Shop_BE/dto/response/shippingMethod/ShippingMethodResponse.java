package com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod;

import com.example.DATN_Fashion_Shop_BE.model.ShippingMethod;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShippingMethodResponse {


    private int id;
    private String methodName;
    private String description;


    public static ShippingMethodResponse fromShippingMethod(ShippingMethod shippingMethod) {
        return ShippingMethodResponse.builder()
                .id(shippingMethod.getId())
                .methodName(shippingMethod.getMethodName())
                .description(shippingMethod.getDescription())
                .build();
    }
}
