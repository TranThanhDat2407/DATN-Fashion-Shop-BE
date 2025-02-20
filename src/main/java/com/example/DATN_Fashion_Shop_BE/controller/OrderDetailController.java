package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.service.OrderDetailService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-details")
@RequiredArgsConstructor
public class OrderDetailController {

    private final OrderDetailService orderDetailService;
    private final LocalizationUtils localizationUtils;

    /**
     * Lấy danh sách chi tiết đơn hàng theo orderId
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderDetailResponse>>> getOrderDetailsByOrderId(@PathVariable Long orderId) {
        List<OrderDetailResponse> orderDetails = orderDetailService.getOrderDetailsByOrderId(orderId);

        // Kiểm tra nếu không có dữ liệu
        if (orderDetails.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDER_DETAILS_NOT_FOUND),
                            null
                    )
            );
        }

        // Trả về danh sách chi tiết đơn hàng
        return ResponseEntity.ok().body(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.ORDER_DETAILS_SUCCESS),
                        orderDetails
                )
        );
    }
}
