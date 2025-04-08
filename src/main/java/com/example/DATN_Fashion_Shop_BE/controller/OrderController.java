package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.GhnCreateOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.PreviewOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.ClickAndCollectOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.UpdateStoreOrderStatusRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.UpdateStorePaymentMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.store.StorePaymentRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnCreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.PreviewOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.TotalOrderTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StorePaymentResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.vnpay.VnPayResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.service.*;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final LocalizationUtils localizationUtils;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final InventoryService inventoryService;
    private final EmailService emailService;





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


        if (responseBody instanceof Order order) {
            log.info("Order detected (COD or Pay in Store). Converting to CreateOrderResponse.");
            CreateOrderResponse createOrderResponse = CreateOrderResponse.fromOrder(order);

            return ResponseEntity.ok(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                    createOrderResponse
            ));
        }


        // Xử lý thanh toán VNPay hoặc MoMo
        if (responseBody instanceof Map<?, ?> paymentResponse) {

            if (paymentResponse.containsKey("paymentUrl")) {
                String paymentUrl = (String) paymentResponse.get("paymentUrl");
                log.info("VNPay payment link response detected: {}", paymentUrl);
            } else if (paymentResponse.containsKey("payUrl")) {
                String payUrl = (String) paymentResponse.get("payUrl");
                log.info("MoMo payment link response detected: {}", payUrl);
            } else {
                log.warn("Unknown payment provider response.");
            }

            return ResponseEntity.ok(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
                    paymentResponse
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
            @RequestParam(defaultValue = "5") int size) {

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



    @Operation(
            summary = "Nhận callback từ VNPAY",
            description = "API này nhận thông báo từ VNPAY để xác nhận giao dịch.",
            tags = "Orders"
    )
    @PostMapping("/return")
    public ResponseEntity<?> handleVNPayReturn(@RequestBody Map<String, String> vnpParams) {

        log.info("📤 [VNPay Callback] Nhận dữ liệu: {}", vnpParams);

        String transactionCode = vnpParams.get("vnp_TxnRef");
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnp_TransactionNo = vnpParams.get("vnp_TransactionNo");
        String vnp_TransactionStatus = vnpParams.get("vnp_TransactionStatus");
        double amount = Double.parseDouble(vnpParams.get("vnp_Amount")) / 100;
        log.info("📌 vnp_TxnRef nhận được từ VNPay: {}", transactionCode);

        // ✅ Xác minh tính hợp lệ của giao dịch
        boolean isValid = vnPayService.verifyPayment(vnpParams);

        if (!isValid) {
            log.error("❌ Thanh toán VNPay không hợp lệ hoặc bị từ chối.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Thanh toán thất bại."));
        }
        // 1️⃣ Kiểm tra mã giao dịch và tìm đơn hàng
        Order order = orderRepository.findById(Long.valueOf(transactionCode))

                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã giao dịch: " + transactionCode));
//        Order order = orderRepository.findOrderWithUserAndAddresses(Long.valueOf(transactionCode))
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã giao dịch: " + transactionCode));

//         ✅ Kiểm tra trạng thái thanh toán VNPay
        if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
            order.setOrderStatus(orderStatusRepository.findByStatusName("PROCESSING")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái PROCESSING.")));

            if (order.getTransactionId() != null) {
                log.warn("⚠ Đơn hàng {} đã có transactionId {}, bỏ qua cập nhật.", order.getId(), order.getTransactionId());
                return ResponseEntity.ok(CreateOrderResponse.fromOrder(order));
            }

            order.setTransactionId(vnp_TransactionNo);
            orderRepository.save(order);

            log.info("✅ Giao dịch thành công. Đã cập nhật trạng thái đơn hàng ID: {}", order.getId());

            boolean paymentExists = paymentRepository.existsByTransactionCode(transactionCode);
            if (paymentExists) {
                log.warn("⚠ Thanh toán đã tồn tại cho đơn hàng ID: {}. Không lưu trùng lặp.", transactionCode);
            } else {
                // 6️⃣ Lưu thông tin thanh toán
                Payment payment = Payment.builder()
                        .order(order)
                        .paymentMethod(paymentMethodRepository.findByMethodName("VNPAY")
                                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ.")))
                        .paymentDate(new Date())
                        .amount(amount)
                        .status("PAID")
                        .transactionCode(vnp_TransactionNo)
                        .build();

                paymentRepository.save(payment);
                log.info("✅ Đã lưu thông tin thanh toán cho đơn hàng ID: {}", transactionCode);
            }
            Order userWithAddresses = orderRepository.findOrderWithUserAndAddresses(Long.valueOf(transactionCode))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã giao dịch: " + transactionCode));



                   List<UserAddressResponse> userAddressResponses = (userWithAddresses.getUser().getUserAddresses() != null)
                    ? userWithAddresses.getUser().getUserAddresses().stream()
                    .map(UserAddressResponse::fromUserAddress)
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            User user = order.getUser();
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {

                List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

                List<OrderDetailResponse> orderDetailResponses = orderDetails.stream()
                        .map(orderDetail -> OrderDetailResponse.fromOrderDetail(orderDetail, userAddressResponses, paymentRepository))
                        .collect(Collectors.toList());



                emailService.sendOrderConfirmationEmail(user.getEmail(), orderDetailResponses);
                log.info("📧 Đã gửi email xác nhận đơn hàng (VNPay) đến {}", user.getEmail());
            } else {
                log.warn("⚠ Không thể gửi email vì email của người dùng không tồn tại.");
            }


        } else {
            log.info("❌ Giao dịch thất bại với vnp_ResponseCode: {} và vnp_TransactionStatus: {}", vnp_ResponseCode, vnp_TransactionStatus);
            Optional<OrderStatus> cancelledStatus = orderStatusRepository.findByStatusName("CANCELLED");
            if (cancelledStatus.isPresent()) {
                order.setOrderStatus(cancelledStatus.get());
                orderRepository.save(order);
                order = orderRepository.findById(order.getId()).orElseThrow();
                log.info("✅ Đã cập nhật trạng thái đơn hàng ID: {} thành CANCELLED", order.getId());
            } else {
                log.error("⚠ Không tìm thấy trạng thái CANCELLED trong database.");
            }

            log.error("❌ Giao dịch thất bại. Đã cập nhật trạng thái đơn hàng ID: {}", order.getId());
        }

        return ResponseEntity.ok(CreateOrderResponse.fromOrder(order));
    }





    @Operation(
            summary = "Lọc đơn hàng theo trạng thái (dùng cho Customers)" ,
            description = "API này cho phép người dùng xem danh sách đơn hàng theo trạng thái",
            tags = "Orders"
    )
    @GetMapping("/history/status")
    public ResponseEntity<ApiResponse<Page<HistoryOrderResponse>>> getOrderHistoryByStatus(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size); // Tạo Pageable trước
        Page<HistoryOrderResponse> historyOrders;

        // Nếu `status` rỗng hoặc null, lấy tất cả đơn hàng, ngược lại lọc theo trạng thái
        if (status == null || status.isEmpty()) {
            historyOrders = orderService.getAllOrders(pageable);
        } else {
            historyOrders = orderService.getOrdersByStatus(status,page,size);
        }

        if (historyOrders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_HISTORY_NOT_FOUND),
                            null
                    )
            );
        }


        return ResponseEntity.ok().body(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_HISTORY_SUCCESS),
                        historyOrders
                )
        );
    }

    @Operation(
            summary = " ✅ Lọc và lấy danh sách đơn hàng theo nhiều tiêu chí (dùng cho Admin)",
            description = "API này cho phép người dùng xem danh sách đơn hàng theo trạng thái",
            tags = "Orders"
    )
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<GetAllOrderAdmin>>> getFilteredOrders(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDateTime fromDate,
            @RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDateTime toDate,
            @RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDateTime updateFromDate,
            @RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime updateToDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Page<GetAllOrderAdmin> orders = orderService.getFilteredOrders(
                orderId, status, shippingAddress, minPrice, maxPrice,
                fromDate, toDate, updateFromDate, updateToDate,
                page, size, sortBy, sortDirection
        );
        log.info("Received shippingAddress: " + shippingAddress);

        return buildResponse(orders, MessageKeys.ORDERS_HISTORY_SUCCESS, MessageKeys.ORDERS_HISTORY_NOT_FOUND);
    }

    // 📌 Hàm dùng chung để trả về API response
    private ResponseEntity<ApiResponse<Page<GetAllOrderAdmin>>> buildResponse(Page<GetAllOrderAdmin> orders, String successKey, String errorKey) {
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(HttpStatus.NOT_FOUND, localizationUtils.getLocalizedMessage(errorKey), null)
            );
        }
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(localizationUtils.getLocalizedMessage(successKey), orders)
        );
    }
    @Operation(
            summary = "Cập nhật trạng thái đơn hàng",
            description = "Cho phép cập nhật trạng thái đơn hàng theo ID",
            tags = "Orders"
    )
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<GetAllOrderAdmin>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        log.info("💳  Received request body: {}", request);


        String status = request.get("status");
        if (status == null || status.isEmpty()) {
            log.error("Status is missing in request body!");
            return ResponseEntity.badRequest().body(ApiResponseUtils.errorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Status is required",null));
        }
        if (!Arrays.asList("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "DONE").contains(status)) {
            return ResponseEntity.badRequest().body(ApiResponseUtils.errorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status value",
                    null));
        }
        GetAllOrderAdmin updatedOrder = orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        "Cập nhật trạng thái đơn hàng thành công",
                        updatedOrder
                )
        );
    }

    @Operation(
            summary = "Cập nhật trạng thái thanh toán",
            description = "Cho phép cập nhật trạng thái thanh toán cho đơn với đối với (COD) dành cho Staff",
            tags = "Orders"
    )
    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<ApiResponse<GetAllOrderAdmin>> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam String paymentStatus) {

        GetAllOrderAdmin updatedOrder = orderService.updatePaymentStatus(orderId, paymentStatus);

        if (updatedOrder == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            localizationUtils.getLocalizedMessage(MessageKeys.ORDER_NOT_FOUND),
                            null
                    )
            );
        }

        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PAYMENT_STATUS_UPDATED_SUCCESS),
                        updatedOrder
                )
        );
    }








    @GetMapping("revenue/today")
    public ResponseEntity<ApiResponse<TotalRevenueTodayResponse>> getRevenueToday() {
        TotalRevenueTodayResponse revenueToday = orderService.getTotalRevenueToday();

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Đã lấy được doanh thu",
                revenueToday
        ));

    }

    @GetMapping("revenue/yesterday")
    public ResponseEntity<ApiResponse<Double>> getRevenueYesterday() {
        Double revenueYesterday = orderService.getTotalRevenueYesterday();

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Đã lấy được doanh thu hom qua",
                revenueYesterday
        ));

    }


    @GetMapping("orderTotal/today")
    public ResponseEntity<ApiResponse<TotalOrderTodayResponse>> getTotalOrderToday() {
        TotalOrderTodayResponse orderToday = orderService.getTotalOrderToday();
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Đã lấy đượcTotalOrderToday ",
                orderToday
        ));
    }
    @GetMapping("orderTotal/yesterday")
    public ResponseEntity<ApiResponse<Integer>> getTotalOrderYesterday() {
        Integer orderYesterday = orderService.getTotalOrderYesterday();
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Đã lấy được getTotalOrderYesterday ",
                orderYesterday
        ));
    }

    @GetMapping("orderCancelTotal/today")
    public ResponseEntity<ApiResponse<TotalOrderCancelTodayResponse>> getTotalOrderCancelToday() {
        TotalOrderCancelTodayResponse orderCancelToday = orderService.getTotalOrderCancelToday();
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Đã lấy được getTotalOrderCancelToday",
                orderCancelToday
        ));
    }

    @GetMapping("orderCancelTotal/yesterday")
    public ResponseEntity<ApiResponse<Integer>> getTotalOrderCancelYesterday() {
        Integer ordercancelYesterday = orderService.getTotalOrderCancelYesterday();
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Đã lấy được getTotalOrderCancelYesterday ",
                ordercancelYesterday
        ));
    }

    @PostMapping("/checkout-store/{staffId}")
    public ResponseEntity<ApiResponse<StorePaymentResponse>> createStoreOrder(
            @PathVariable Long staffId,
            @Valid @RequestBody StorePaymentRequest request) throws DataNotFoundException {

        StorePaymentResponse response = orderService.createStoreOrder(staffId, request);

        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        MessageKeys.ORDERS_SUCCESSFULLY,
                        response
                )
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<PageResponse<StoreOrderResponse>>> getOrders(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long orderStatusId,
            @RequestParam(required = false) Long paymentMethodId,
            @RequestParam(required = false) Long shippingMethodId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "vi") String languageCode,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Lấy danh sách đơn hàng đã chuyển đổi
        Page<StoreOrderResponse> storeOrders = orderService.getStoreOrdersByFilters(
                storeId, orderStatusId, paymentMethodId, shippingMethodId, customerId, staffId, startDate, endDate, languageCode, pageable
        );

        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        MessageKeys.ORDERS_SUCCESSFULLY,
                        PageResponse.fromPage(storeOrders)
                )
        );
    }

    @GetMapping("store/order-detail/{orderId}")
    public ResponseEntity<ApiResponse<StoreOrderResponse>> getStoreOrderById(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "vi") String languageCode
    ) throws DataNotFoundException {
        StoreOrderResponse response = orderService.getStoreOrderById(orderId, languageCode);

        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        MessageKeys.ORDERS_SUCCESSFULLY,
                        response
                )
        );
    }

    @PutMapping("store/{orderId}/status")
    public ResponseEntity<ApiResponse<?>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateStoreOrderStatusRequest request) throws DataNotFoundException {
        orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        MessageKeys.ORDERS_SUCCESSFULLY,
                        "success"
                )
        );
    }

    // Cập nhật phương thức thanh toán
    @PutMapping("store/{orderId}/payment-method")
    public ResponseEntity<ApiResponse<?>> updatePaymentMethod(
            @PathVariable Long orderId,
            @RequestBody UpdateStorePaymentMethodRequest request) throws DataNotFoundException {
        orderService.updatePaymentMethod(orderId, request);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        MessageKeys.ORDERS_SUCCESSFULLY,
                        "success"
                )
        );
    }

//    @Operation(
//            summary = "Đặt hàng Click & Collect",
//            description = "API này cho phép người dùng đặt hàng Click & Collect, kiểm tra tồn kho và xử lý thanh toán.",
//            tags = "Orders"
//    )
//    @PostMapping("/create-click-and-collect-order")
//    public ResponseEntity<ApiResponse<?>> createClickAndCollectOrder(
//            HttpServletRequest request,
//            @RequestBody @Valid ClickAndCollectOrderRequest orderRequest,
//            BindingResult bindingResult) {
//
//        // 1️⃣ Kiểm tra lỗi đầu vào
//        if (bindingResult.hasErrors()) {
//            log.debug("Validation errors: " + bindingResult.getAllErrors());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    ApiResponseUtils.generateValidationErrorResponse(
//                            bindingResult,
//                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_CREATE_FAILED),
//                            localizationUtils
//                    )
//            );
//        }
//
//        // 2️⃣ Gọi service để tạo đơn hàng Click & Collect
//        ResponseEntity<?> response = orderService.createClickAndCollectOrder(orderRequest, request);
//        Object responseBody = response.getBody();
//
//        // 3️⃣ Nếu response body null => thất bại
//        if (responseBody == null) {
//            log.error("Click & Collect order creation failed, response body is null");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    ApiResponseUtils.errorResponse(
//                            HttpStatus.BAD_REQUEST,
//                            localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_CREATE_FAILED),
//                            "clickAndCollectOrder",
//                            null,
//                            "Không thể tạo đơn hàng Click & Collect, vui lòng thử lại sau."
//                    )
//            );
//        }
//
//        // 4️⃣ Nếu VNPay trả về Map (Link thanh toán)
//        if (responseBody instanceof Map<?, ?> paymentResponse) {
//            log.info("VNPay payment link response detected.");
//            return ResponseEntity.ok(ApiResponseUtils.successResponse(
//                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
//                    paymentResponse
//            ));
//        }
//
//        // 5️⃣ Nếu đơn hàng tạo thành công theo phương thức thanh toán tại cửa hàng
//        if (responseBody instanceof Order order) {
//            log.info("Click & Collect order with Pay in Store detected. Converting to CreateOrderResponse.");
//            CreateOrderResponse createOrderResponse = CreateOrderResponse.fromOrder(order);
//            log.debug("Converted CreateOrderResponse: " + createOrderResponse);
//
//            return ResponseEntity.ok(ApiResponseUtils.successResponse(
//                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
//                    createOrderResponse
//            ));
//        }
//
//        // 6️⃣ Nếu response là CreateOrderResponse
//        if (responseBody instanceof CreateOrderResponse createOrderResponse) {
//            log.info("CreateOrderResponse detected, returning success response.");
//            return ResponseEntity.ok(ApiResponseUtils.successResponse(
//                    localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
//                    createOrderResponse
//            ));
//        }
//
//        // 7️⃣ Nếu không khớp bất kỳ điều kiện nào
//        log.error("Unexpected response type: " + responseBody.getClass().getName());
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                ApiResponseUtils.errorResponse(
//                        HttpStatus.BAD_REQUEST,
//                        localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_CREATE_FAILED),
//                        "clickAndCollectOrder",
//                        null,
//                        "Không thể tạo đơn hàng Click & Collect, vui lòng thử lại sau."
//                )
//        );
//    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrderAndRestoreInventory(
            @PathVariable Long orderId
            ) throws DataNotFoundException {

        // 1. Kiểm tra đơn hàng có thể hủy
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));


        // 2. Hủy đơn hàng
        orderService.cancelOrder(orderId);

        // 3. Hoàn trả tồn kho
        inventoryService.restoreInventoryFromCancelledOrder(orderId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Order cancelled successfully and inventory restored",
                "Order " + orderId + " has been cancelled"
        ));
    }
}


