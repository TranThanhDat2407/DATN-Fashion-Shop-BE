package com.example.DATN_Fashion_Shop_BE.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.model.OrderDetail;
import com.example.DATN_Fashion_Shop_BE.repository.OrderDetailRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderDetailService {


    private final OrderDetailRepository orderDetailRepository;

    /**
     * Lấy danh sách chi tiết đơn hàng theo orderId
     */
    public List<OrderDetailResponse> getOrderDetailsByOrderId(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

        // Chuyển đổi sang DTO
        return orderDetails.stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .collect(Collectors.toList());
    }
}
