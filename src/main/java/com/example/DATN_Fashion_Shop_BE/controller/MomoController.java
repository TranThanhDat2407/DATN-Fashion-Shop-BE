package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.CreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.example.DATN_Fashion_Shop_BE.model.OrderDetail;
import com.example.DATN_Fashion_Shop_BE.model.Payment;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.service.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/momo")
@AllArgsConstructor
public class MomoController {
    private static final Logger log = LoggerFactory.getLogger(MomoController.class);
    private final MomoService momoService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final EmailService emailService;
    /**
     * Tạo yêu cầu thanh toán MoMo
     * @param amount Số tiền thanh toán
     * @param orderInfo Thông tin đơn hàng
     * @param orderId Mã đơn hàng
     * @return Dữ liệu yêu cầu thanh toán
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestParam long amount,
            @RequestParam String orderInfo,
            @RequestParam String orderId) {

        try {
            log.info("Tạo yêu cầu thanh toán MoMo - Amount: {}, OrderInfo: {}, OrderId: {}",
                    amount, orderInfo, orderId);

            Map<String, Object> paymentData = momoService.createPayment(amount, orderInfo, orderId);
            return ResponseEntity.ok(paymentData);

        } catch (Exception e) {
            log.error("Lỗi khi tạo thanh toán MoMo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Lỗi khi tạo thanh toán MoMo: " + e.getMessage()
                    ));
        }
    }

    /**
     * Endpoint nhận callback từ MoMo
     * @param callbackData Dữ liệu callback từ MoMo
     * @return Kết quả xử lý callback
     */
    @PostMapping("/callback")
    public ResponseEntity<?> momoCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            log.info("🔄 Nhận callback từ MoMo lần thứ: {}", System.currentTimeMillis());

            // Xác minh chữ ký từ MoMo
            if (!momoService.verifyCallback(callbackData)) {
                log.warn("❌ Callback không hợp lệ từ MoMo");
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            String resultCode = callbackData.get("resultCode").toString();
            String momoOrderId  = callbackData.get("orderId").toString();
            String transactionId = callbackData.get("transId").toString();
            double amount = Double.parseDouble(callbackData.get("amount").toString());

            log.info("📌 orderId nhận được từ MoMo: {}", momoOrderId);

            String[] orderIdParts = momoOrderId .split("_");
            long orderId = Long.parseLong(orderIdParts[0]);

            // Tìm đơn hàng
            Order order = orderRepository.findOrderWithUserAndAddresses(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã giao dịch: " + orderId));

            if ("0".equals(resultCode)) {

                order.setOrderStatus(orderStatusRepository.findByStatusName("PROCESSING")
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái PROCESSING.")));

                order.setTransactionId(transactionId);
                orderRepository.save(order);


//                log.info("✅ Giao dịch thành công. Đã cập nhật trạng thái đơn hàng ID: {}", order.getId());
//             boolean paymentExists = paymentRepository.existsByOrderId(orderId);
//                if (paymentExists) {
//                    log.warn("⚠ Thanh toán đã tồn tại cho đơn hàng ID: {}. Không lưu trùng lặp.", orderId);
//                } else {
                if(!paymentRepository.existsByOrderId(orderId)) {
                    // Lưu thông tin thanh toán
                    Payment payment = Payment.builder()
                            .order(order)
                            .paymentMethod(paymentMethodRepository.findByMethodName("MOMO")
                                    .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ.")))
                            .paymentDate(new Date())
                            .amount(amount)
                            .status("PAID")
                            .transactionCode(transactionId)
                            .build();

                    paymentRepository.save(payment);
                    log.info("✅ Đã lưu thông tin thanh toán cho đơn hàng ID: {}", orderId);


                }
                order = orderRepository.findOrderWithUserAndAddresses(orderId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng sau khi lưu payment: " + orderId));

                List<UserAddressResponse> userAddressResponses = (order.getUser().getUserAddresses() != null)
                        ? order.getUser().getUserAddresses().stream()
                        .map(UserAddressResponse::fromUserAddress)
                        .collect(Collectors.toList())
                        : new ArrayList<>();


                User user = order.getUser();
                if (order.getUser().getEmail() != null && !order.getUser().getEmail().isEmpty()) {

                    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

                    List<OrderDetailResponse> orderDetailResponses = orderDetails.stream()
                            .map(orderDetail -> OrderDetailResponse.fromOrderDetail(orderDetail, userAddressResponses, paymentRepository))
                            .collect(Collectors.toList());

                    emailService.sendOrderConfirmationEmail(user.getEmail(), orderDetailResponses);
                    log.info("📧 Đã gửi email xác nhận đơn hàng (MoMo) đến {}", user.getEmail());
                } else {
                    log.warn("⚠ Không thể gửi email vì email của người dùng không tồn tại.");
                }

            } else {
                log.info("❌ Giao dịch thất bại với resultCode: {}", resultCode);
                order.setOrderStatus(orderStatusRepository.findByStatusName("CANCELLED")
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái CANCELLED.")));
                orderRepository.save(order);
                log.info("✅ Đã cập nhật trạng thái đơn hàng ID: {} thành CANCELLED", order.getId());
            }

            return ResponseEntity.ok(CreateOrderResponse.fromOrder(order));

        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý callback từ MoMo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Lỗi khi xử lý callback từ MoMo",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Truy vấn trạng thái thanh toán
     * @param orderId Mã đơn hàng
     * @param requestId Mã yêu cầu
     * @return Trạng thái thanh toán
     */
    @GetMapping("/query-status")
    public ResponseEntity<?> queryPaymentStatus(
            @RequestParam String orderId,
            @RequestParam String requestId) {

        try {
            log.info("Truy vấn trạng thái thanh toán - OrderId: {}, RequestId: {}",
                    orderId, requestId);

            Map<String, Object> queryResult = momoService.queryPaymentStatus(orderId, requestId);
            return ResponseEntity.ok(queryResult);

        } catch (Exception e) {
            log.error("Lỗi khi truy vấn trạng thái thanh toán MoMo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Lỗi khi truy vấn trạng thái thanh toán: " + e.getMessage()
                    ));
        }
    }

    /**
     * Kiểm tra kết nối MoMo
     * @return Kết quả kiểm tra
     */
    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        try {
            // Tạo một yêu cầu test đơn giản
            String requestId = momoService.generateRequestId();
            Map<String, Object> testQuery = momoService.queryPaymentStatus("TEST_ORDER", requestId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Kết nối MoMo thành công",
                    "testData", testQuery
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Lỗi kết nối MoMo: " + e.getMessage()
                    ));
        }
    }

}
