package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.CreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.OrderPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.example.DATN_Fashion_Shop_BE.service.OrderService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final LocalizationUtils localizationUtils;

    @Operation(
            summary = "Đặt hàng",
            description = "API này cho phép người dùng đặt hàng, bao gồm thông tin đơn hàng và phương thức thanh toán.",
            tags = "Orders"
    )
    @PostMapping("/place-order")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> placeOrder(
            @RequestBody @Valid OrderRequest orderRequest,
            BindingResult bindingResult) {

        // Kiểm tra lỗi đầu vào
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                            localizationUtils
                    )
            );
        }


            // Gọi service để tạo đơn hàng
            Order order = orderService.placeOrder(orderRequest);

            // Tạo response từ đối tượng Order
            CreateOrderResponse createOrderResponse = CreateOrderResponse.fromOrder(order);

            // Trả về phản hồi thành công với thông tin đơn hàng
            return ResponseEntity.ok().body(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                    createOrderResponse
            ));


    }


    @Operation(
            summary = "Xem trước đơn hàng",
            description = "API này cho phép người dùng xem trước tổng giá trị đơn hàng, bao gồm tổng giỏ hàng, thuế, phí vận chuyển, và giá trị cuối cùng trước khi đặt hàng.",
            tags = "Orders"
    )
    @PostMapping("/order-preview")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> getOrderPreview(
            @RequestBody @Valid OrderRequest orderRequest,
            BindingResult bindingResult) {

        // Kiểm tra lỗi đầu vào
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_PREVIEW_FAILED),
                            localizationUtils
                    )
            );
        }

        try {
            // Gọi service để tính toán tổng giá trị đơn hàng dự kiến
            OrderPreviewResponse orderPreview = orderService.getOrderPreview(orderRequest);

            // Trả về phản hồi thành công với thông tin tổng giá trị đơn hàng dự kiến
            return ResponseEntity.ok().body(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_PREVIEW_SUCCESS),
                    orderPreview
            ));

        } catch (Exception e) {
            // Nếu có lỗi trong quá trình tính toán, trả về lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_PREVIEW_FAILED),
                            "exception",
                            e.getMessage(),
                            e.getMessage()
                    )
            );
        }
    }

}
