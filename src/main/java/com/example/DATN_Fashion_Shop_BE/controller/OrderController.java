package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.GhnCreateOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.PreviewOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnCreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.PreviewOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.CreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.OrderPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.example.DATN_Fashion_Shop_BE.model.OrderStatus;
import com.example.DATN_Fashion_Shop_BE.repository.OrderRepository;
import com.example.DATN_Fashion_Shop_BE.repository.PaymentRepository;
import com.example.DATN_Fashion_Shop_BE.service.OrderService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final LocalizationUtils localizationUtils;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Operation(
            summary = "Đặt hàng",
            description = "API này cho phép người dùng đặt hàng, bao gồm thông tin đơn hàng và phương thức thanh toán.",
            tags = "Orders"
    )

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
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
        ResponseEntity<?> response = orderService.createOrder(orderRequest);

        // Kiểm tra nếu trả về URL thanh toán VNPay
        if (response.getBody() instanceof Map) {
            return response; // Trả về luôn URL thanh toán VNPay
        }

        // Nếu không phải VNPay, tức là đơn hàng COD → Ép kiểu sang Order
        Order order = (Order) response.getBody();
        CreateOrderResponse createOrderResponse = CreateOrderResponse.fromOrder(order);

        return ResponseEntity.ok().body(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                createOrderResponse
        ));
    }


    @Operation(
            summary = "Xem trước đơn hàng",
            description = "API này cho phép người dùng xem trước phí vận chuyển trước khi đặt hàng.",
            tags = "Orders"
    )
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<PreviewOrderResponse>> previewOrder(
            @RequestBody @Valid PreviewOrderRequest previewOrderRequest,
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

        // Gọi service để lấy thông tin dự kiến đơn hàng từ GHN
        PreviewOrderResponse previewResponse = orderService.previewOrder(previewOrderRequest);

        // Trả về phản hồi thành công với thông tin preview
        return ResponseEntity.ok().body(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_PREVIEW_SUCCESS),
                previewResponse
        ));
    }



//    @Operation(
//            summary = "Tạo đơn hàng GHN",
//            description = "API này cho phép người dùng tạo đơn hàng trên GHN.",
//            tags = "Orders"
//    )
//    @PostMapping("/create")
//    public ResponseEntity<ApiResponse<GhnCreateOrderResponse>> createOrder(
//            @RequestBody @Valid GhnCreateOrderRequest ghnCreateOrderRequest,
//            BindingResult bindingResult) {
//
//        // Kiểm tra lỗi đầu vào
//        if (bindingResult.hasErrors()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    ApiResponseUtils.generateValidationErrorResponse(
//                            bindingResult,
//                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_CREATE_FAILED),
//                            localizationUtils
//                    )
//            );
//        }
//
//        // Gọi service để tạo đơn hàng GHN
//        GhnCreateOrderResponse createOrderResponse = orderService.createOrder(ghnCreateOrderRequest);
//
//        // Trả về phản hồi thành công với thông tin đơn hàng đã tạo
//        return ResponseEntity.ok().body(ApiResponseUtils.successResponse(
//                localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
//                createOrderResponse
//        ));
//    }

    @GetMapping("/payment/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> vnpParams) {
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnp_TxnRef = vnpParams.get("vnp_TxnRef");

        Optional<Order> orderOptional = orderRepository.findById(Long.parseLong(vnp_TxnRef));
        if (orderOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Đơn hàng không tồn tại.");
        }

        Order order = orderOptional.get();
        if ("00".equals(vnp_ResponseCode)) { // "00" là mã thành công của VNPay
            order.setOrderStatus(OrderStatus.builder().id(2L).build()); // Đã thanh toán
            paymentRepository.updatePaymentStatus(order.getId(), "SUCCESS");
            orderRepository.save(order);
            return ResponseEntity.ok("Thanh toán thành công.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thanh toán thất bại.");
        }
    }
}


