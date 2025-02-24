package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.GhnCreateOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.PreviewOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnCreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.PreviewOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.CreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.HistoryOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.OrderPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.example.DATN_Fashion_Shop_BE.model.OrderStatus;
import com.example.DATN_Fashion_Shop_BE.model.Payment;
import com.example.DATN_Fashion_Shop_BE.repository.CartItemRepository;
import com.example.DATN_Fashion_Shop_BE.repository.CartRepository;
import com.example.DATN_Fashion_Shop_BE.repository.OrderRepository;
import com.example.DATN_Fashion_Shop_BE.repository.PaymentRepository;
import com.example.DATN_Fashion_Shop_BE.service.CartService;
import com.example.DATN_Fashion_Shop_BE.service.OrderService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final LocalizationUtils localizationUtils;
    private static final String HASH_SECRET = "HJF2G7EHCHPX0K446LBH17FKQUF56MB5";
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final VnPayController vnPayController;


    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    @Operation(
            summary = "Đặt hàng",
            description = "API này cho phép người dùng đặt hàng, bao gồm thông tin đơn hàng và phương thức thanh toán.",
            tags = "Orders"
    )
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<?>> createOrder(HttpServletRequest request,
                                                      @RequestBody @Valid OrderRequest orderRequest,
                                                      BindingResult bindingResult) {
        // Kiểm tra lỗi đầu vào
        if (bindingResult.hasErrors()) {
            log.debug("Validation errors: " + bindingResult.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                            localizationUtils
                    )
            );
        }

        // Gọi service để tạo đơn hàng
        ResponseEntity<?> response = orderService.createOrder(orderRequest,request);
        Object responseBody = response.getBody();


        // Trường hợp response body null
        if (responseBody == null) {
            log.error("Order creation failed, response body is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_CREATE_FAILED),
                            "order",
                            null,
                            "Không thể tạo đơn hàng, vui lòng thử lại sau."
                    )
            );
        }

        // Trường hợp VNPay trả về Map (Link thanh toán)
        if (responseBody instanceof Map<?, ?> paymentResponse) {
            log.info("VNPay payment link response detected.");
            return ResponseEntity.ok(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                    paymentResponse
            ));
        }

        // Trường hợp COD: response.getBody() là Order
        if (responseBody instanceof Order order) {
            log.info("Cash on Delivery (COD) order detected. Converting to CreateOrderResponse.");
            CreateOrderResponse createOrderResponse = CreateOrderResponse.fromOrder(order);
            log.debug("Converted CreateOrderResponse: " + createOrderResponse);

            return ResponseEntity.ok(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                    createOrderResponse
            ));
        }


        if (responseBody instanceof CreateOrderResponse createOrderResponse) {
            log.info("CreateOrderResponse detected, returning success response.");
            return ResponseEntity.ok(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                    createOrderResponse
            ));
        }

        // Nếu không khớp bất kỳ điều kiện nào
        log.error("Unexpected response type: " + responseBody.getClass().getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseUtils.errorResponse(
                        HttpStatus.BAD_REQUEST,
                        localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_CREATE_FAILED),
                        "order",
                        null,
                        "Không thể tạo đơn hàng, vui lòng thử lại sau."
                )
        );
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


    @Operation(
            summary = "Lấy lịch sử đơn hàng",
            description = "API này cho phép người dùng xem danh sách đơn hàng đã đặt theo userId.",
            tags = "Orders"
    )
    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<Page<HistoryOrderResponse>>> getOrderHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<HistoryOrderResponse> historyOrders = orderService.getOrderHistoryByUserId(userId, page, size);

        // Nếu không có đơn hàng nào
        if (historyOrders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_HISTORY_NOT_FOUND),
                            null

                    )
            );
        }

        // Trả về danh sách lịch sử đơn hàng có phân trang
        return ResponseEntity.ok().body(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_HISTORY_SUCCESS),
                        historyOrders
                )
        );
    }



    private String hashAndBuildUrl(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                hashData.append(fieldName).append("=").append(value).append("&");
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                        .append("&");
            }
        }

        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        if (query.length() > 0) query.setLength(query.length() - 1);

        // Tạo SecureHash đúng chuẩn
        String secureHash = hmacSHA512(HASH_SECRET, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return query.toString();
    }


    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa HmacSHA512", e);
        }
    }



}


