package com.example.DATN_Fashion_Shop_BE.dto.response.order;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
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
        UserAddress shippingAddress = user.getUserAddresses().stream()
                .filter(UserAddress::getIsDefault)
                .findFirst()
                .orElse(null);

        String customerName = (shippingAddress != null) ? shippingAddress.getFirstName() + " "
                + shippingAddress.getLastName() : "Tạm thời khách hàng chưa cập nhật tên";
        String customerPhone = (shippingAddress != null) ? shippingAddress.getPhone() :
                "Tạm thời không có số điện thoại";


        return GetAllOrderAdmin.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .totalAmount(order.getTotalAmount())
                .orderTime(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().getStatusName())
                .paymentStatus(paymentStatus)
                .shippingAddress(order.getShippingAddress())
                .customerName(customerName)
                .customerPhone(customerPhone)
                .build();
    }

}
