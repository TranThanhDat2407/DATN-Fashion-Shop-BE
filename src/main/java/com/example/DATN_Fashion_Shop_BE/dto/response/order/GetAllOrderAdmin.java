package com.example.DATN_Fashion_Shop_BE.dto.response.order;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllOrderAdmin {

    private Long orderId;
    private Double totalPrice;
    private Double totalAmount;
    private String orderStatus;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime orderTime;
    private String shippingAddress;
    private String paymentStatus;
    private String customerName;
    private String customerPhone;

    public static GetAllOrderAdmin fromGetAllOrderAdmin(Order order) {



        String paymentStatus = order.getPayments().stream()
                .findFirst()
                .map(Payment::getStatus)
                .orElse("");



        User user = order.getUser();
        boolean isGuest = (user == null);

        UserAddress shippingAddress = (!isGuest && user.getUserAddresses() != null) ?
                user.getUserAddresses().stream()
                        .filter(UserAddress::getIsDefault)
                        .findFirst()
                        .orElse(null)
                : null;

        String customerName = isGuest ? "Guest" :
                (shippingAddress != null ? shippingAddress.getFirstName() + " " + shippingAddress.getLastName() : "Unknown");


        String customerPhone = (shippingAddress != null) ? shippingAddress.getPhone() : "Guest";



        return GetAllOrderAdmin.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .totalAmount(order.getTotalAmount())
                .orderTime(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().getStatusName())
                .paymentStatus(paymentStatus)
                .shippingAddress(order.getShippingAddress() != null ? order.getShippingAddress() : "Không có địa chỉ")
                .customerName(customerName)
                .customerPhone(customerPhone)
                .build();
    }

}
