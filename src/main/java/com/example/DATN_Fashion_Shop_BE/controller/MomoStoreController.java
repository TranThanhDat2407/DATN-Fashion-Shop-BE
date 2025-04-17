package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.dto.response.store.StorePaymentResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.example.DATN_Fashion_Shop_BE.model.Payment;
import com.example.DATN_Fashion_Shop_BE.repository.OrderRepository;
import com.example.DATN_Fashion_Shop_BE.repository.OrderStatusRepository;
import com.example.DATN_Fashion_Shop_BE.repository.PaymentMethodRepository;
import com.example.DATN_Fashion_Shop_BE.repository.PaymentRepository;
import com.example.DATN_Fashion_Shop_BE.service.MomoStoreService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/store/momo")
@AllArgsConstructor
public class MomoStoreController {

    private static final Logger log = LoggerFactory.getLogger(MomoStoreController.class);
    private final MomoStoreService momoStoreService;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;


    @PostMapping("/callback")
    public ResponseEntity<?> handleMomoCallback(@RequestBody  Map<String, Object> callbackData) {
        log.info("🔁 Nhận callback từ MoMo Store: {}", callbackData);


        if (!momoStoreService.verifyCallback(callbackData)) {
            log.warn("❌ Callback không hợp lệ từ MoMo");
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        String resultCode = callbackData.get("resultCode").toString();
        log.info("Kết quả giao dịch từ MoMo: {}", resultCode);
        String momoOrderId = (String) callbackData.get("orderId");
        String transactionId = callbackData.get("transId").toString();
        double amount = Double.parseDouble(callbackData.get("amount").toString());


        String[] orderIdParts = momoOrderId.split("_");
        long orderId = Long.parseLong(orderIdParts[0]);


        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("❌ Không tìm thấy đơn hàng với ID: {}", orderId);
            return ResponseEntity.badRequest().body("Order not found");
        }

        try {
            if ("0".equals(resultCode)) {
                order.setOrderStatus(orderStatusRepository.findByStatusName("DONE")
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái DONE.")));
                order.setTransactionId(transactionId);
                orderRepository.save(order);
                log.info("✅ Thanh toán thành công cho orderId: {}", orderId);
          if (!paymentRepository.existsByOrderId(orderId)) {
                    log.info("Kiểm tra thanh toán cho orderId: {}", orderId);
                    log.info("Có thanh toán hay chưa: {}", paymentRepository.existsByOrderId(orderId));

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
                } else {
                    order.setOrderStatus(orderStatusRepository.findByStatusName("CANCELLED")
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái CANCELLED.")));

                    orderRepository.save(order);
                    log.info("❌ Thanh toán thất bại (resultCode: {}), đã cập nhật trạng thái đơn hàng {} thành CANCELLED",
                            resultCode, orderId);
                }

                return ResponseEntity.ok(StorePaymentResponse.fromOrder(order));

            }catch(Exception e){
                log.error("❌ Lỗi khi xử lý callback từ MoMo: ", e);
                return ResponseEntity.internalServerError().body("Error processing callback");
            }


        }
    }

