package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailAdminResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.repository.OrderRepository;
import com.example.DATN_Fashion_Shop_BE.repository.PaymentRepository;
import com.example.DATN_Fashion_Shop_BE.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.model.OrderDetail;
import com.example.DATN_Fashion_Shop_BE.repository.OrderDetailRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderDetailService {


    private final OrderDetailRepository orderDetailRepository;
    private final UserAddressRepository userAddressRepository;
    private final PaymentRepository paymentRepository;
    /**
     * Lấy danh sách chi tiết đơn hàng theo orderId (Customer)
     */
    public List<OrderDetailResponse> getOrderDetailsByOrderId(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

        if (orderDetails.isEmpty()) {
            return Collections.emptyList(); // Trả về danh sách rỗng nếu không có dữ liệu
        }

        // ✅ Lấy userId từ đơn hàng
        Long userId = orderDetails.get(0).getOrder().getUser().getId();

        // ✅ Lấy danh sách địa chỉ của user
        List<UserAddressResponse> userAddressResponses = userAddressRepository.findByUserId(userId)
                .stream()
                .map(UserAddressResponse::fromUserAddress)
                .collect(Collectors.toList());

        // ✅ Chuyển đổi sang DTO
        return orderDetails.stream()
                .map(orderDetail -> OrderDetailResponse.fromOrderDetail(orderDetail, userAddressResponses,paymentRepository))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách chi tiết đơn hàng theo orderId (Admin)
     */
    public List<OrderDetailAdminResponse> getOrderDetailsByOrderIdAdmin(Long orderId) {

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

        if (orderDetails.isEmpty()) {
            return Collections.emptyList();
        }


        return orderDetails.stream()
                .map(OrderDetailAdminResponse::fromOrderDetailAdmin)
                .collect(Collectors.toList());
    }




}
