package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.config.GHNConfig;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.PreviewOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.PreviewOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.CreateOrderResponse;

import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final LocalizationUtils localizationUtils;
    private final RestTemplate restTemplate;
    private final GHNConfig ghnConfig;
    private final VNPayService vnPayService;
    private final UserAddressRepository userAddressRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final GHNService ghnService;
    private final AddressRepository addressRepository;
    private final OrderStatusRepository orderStatusRepository;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    public ResponseEntity<?> createOrder(
            OrderRequest orderRequest, HttpServletRequest request) {
        log.info("🛒 Bắt đầu tạo đơn hàng cho userId: {}", orderRequest.getUserId());
        // 🛒 1️⃣ Lấy giỏ hàng của user
        Cart cart = cartRepository.findByUser_Id(orderRequest.getUserId())
                .orElseThrow(() -> {
                    log.error("❌ Không tìm thấy giỏ hàng của userId: {}", orderRequest.getUserId());
                    return new RuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.CART_NOT_FOUND, orderRequest.getUserId()));
                });

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            log.error("❌ Không có sản phẩm trong giỏ hàng của userId: {}", orderRequest.getUserId());
            throw new RuntimeException(localizationUtils
                    .getLocalizedMessage(MessageKeys.CART_ITEM_NOT_FOUND, cart.getId()));
        }
        log.info("✅ Tìm thấy {} sản phẩm trong giỏ hàng.", cartItems.size());

        // 🏷️ 2️⃣ Tính tổng tiền sản phẩm
        double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getProductVariant().getSalePrice() * item.getQuantity())
                .sum();
        log.info("💰 Tổng tiền sản phẩm: {}", totalAmount);
        // 🎟️ 3️⃣ Áp dụng mã giảm giá (nếu có)
        double discount = 0.0;
        Coupon coupon = null;

        if (orderRequest.getCouponId() != null) {
            coupon = couponRepository.findById(orderRequest.getCouponId())
                    .orElseThrow(() -> {
                        log.error("❌ Mã giảm giá không hợp lệ: {}", orderRequest.getCouponId());
                        return new RuntimeException("Mã giảm giá không hợp lệ.");
                    });

            if (!coupon.getIsActive() || coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
                log.error("❌ Mã giảm giá {} đã hết hạn hoặc không hợp lệ.", orderRequest.getCouponId());
                throw new RuntimeException("Mã giảm giá đã hết hạn hoặc không hợp lệ.");
            }

            discount = coupon.getDiscountValue();
            log.info("🎟️ Mã giảm giá hợp lệ, giảm giá: {}", discount);
        }
        discount = Math.min(discount, totalAmount);

        // 📍 5️⃣ Xử lý địa chỉ giao hàng
        Address address;
        if (orderRequest.getShippingAddress() != null) {
            address = addressRepository.findById(orderRequest.getShippingAddress())
                    .orElseThrow(() -> {
                        log.error("❌ Không tìm thấy địa chỉ giao hàng với ID: {}", orderRequest.getShippingAddress());
                        return new RuntimeException("Không tìm thấy địa chỉ được chọn.");
                    });
        } else {
            UserAddress userAddress = userAddressRepository.findTopByUser_IdAndIsDefaultTrue(orderRequest.getUserId())
                    .orElseThrow(() -> {
                        log.error("❌ Không tìm thấy địa chỉ mặc định của userId: {}", orderRequest.getUserId());
                        return new RuntimeException("Không tìm thấy địa chỉ mặc định của người dùng.");
                    });
            address = userAddress.getAddress();
        }
        String fullShippingAddress = String.format("%s, %s, %s, %s",
                address.getStreet(), address.getWard(), address.getDistrict(), address.getCity());
        log.info("📍 Địa chỉ giao hàng: {}, {}, {}, {}", address.getStreet(), address.getWard(), address.getDistrict(), address.getCity());

        // 🚚 6️⃣ Tính phí vận chuyển
        double shippingFee = ghnService.calculateShippingFee(address, cartItems);
        log.info("🚚 Phí vận chuyển: {}", shippingFee);

        // 🛍 7️⃣ Tính tổng tiền đơn hàng
        double finalAmount = totalAmount - discount + shippingFee;
        log.info("💰 Tổng tiền đơn hàng sau khi áp dụng mã giảm giá và phí vận chuyển: {}", finalAmount);

        OrderStatus orderStatus = orderStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không hợp lệ."));


        ShippingMethod shippingMethod = shippingMethodRepository.findById(orderRequest.getShippingMethodId())
                .orElseThrow(() -> {
                    log.error("❌ Không tìm thấy phương thức vận chuyển hợp lệ với ID: {}", orderRequest.getShippingMethodId());
                    return new RuntimeException("Không tìm thấy phương thức vận chuyển hợp lệ.");
                });



        // 📦 8️⃣ Tạo Order
        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalPrice(totalAmount)
                .totalAmount(finalAmount)
                .orderStatus(orderStatus)
                .shippingAddress(fullShippingAddress)
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(0.0)
                .payments(new ArrayList<>())
                .build();
        log.info("📦 Đơn hàng được tạo nhưng chưa lưu vào database.");


        try {
            Order savedOrder = orderRepository.save(order);
            log.info("✅ Đơn hàng đã lưu thành công với ID: {}", savedOrder.getId());

            // 🛒 9️⃣ Lưu OrderDetail
            List<OrderDetail> orderDetails = cartItems.stream().map(item ->
                    OrderDetail.builder()
                            .order(savedOrder)
                            .product(item.getProductVariant().getProduct())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getProductVariant().getSalePrice())
                            .totalPrice(item.getProductVariant().getSalePrice() * item.getQuantity())
                            .build()).collect(Collectors.toList());

            orderDetailRepository.saveAll(orderDetails);
            log.info("✅ Đã lưu {} sản phẩm vào OrderDetail.", orderDetails.size());

            // 💳 🔟 Xử lý thanh toán
            PaymentMethod paymentMethod = paymentMethodRepository.findById(orderRequest.getPaymentMethodId())
                    .orElseThrow(() -> {
                        log.error("❌ Phương thức thanh toán không hợp lệ với ID: {}", orderRequest.getPaymentMethodId());
                        return new RuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.PAYMENT_METHOD_NOT_VALID));
                    });

            Payment payment = Payment.builder()
                    .order(savedOrder)
                    .paymentMethod(paymentMethod)
                    .paymentDate(new Date())
                    .amount(finalAmount)
                    .status("PENDING")
                    .transactionCode(UUID.randomUUID().toString())
                    .build();



            paymentRepository.save(payment);
            if (savedOrder.getPayments() == null) {
                savedOrder.setPayments(new ArrayList<>());
            }
            savedOrder.getPayments().add(payment); // Cập nhật danh sách thanh toán
            orderRepository.save(savedOrder); // Lưu lại đơn hàng
            log.info("✅ Đã lưu thông tin thanh toán.");

            if ("COD".equalsIgnoreCase(paymentMethod.getMethodName())) {
                log.info("🛒 Đơn hàng {} sẽ thanh toán khi nhận hàng (COD).", savedOrder.getId());
                // 🛒 🔟 Xóa giỏ hàng sau khi đặt hàng thành công
                cartItemRepository.deleteAll(cartItems);
                cartRepository.delete(cart);
                log.info("✅ Giỏ hàng đã được xóa sau khi đặt hàng.");

            } if ("VNPAY".equalsIgnoreCase(paymentMethod.getMethodName())) {
                String vnp_TxnRef = String.valueOf(savedOrder.getId());
                long vnp_Amount = (long) (finalAmount * 100); // Đảm bảo kiểu dữ liệu là long
                String vnp_IpAddr = request.getRemoteAddr();
                String vnp_OrderInfo = "Thanh toán đơn hàng " + vnp_TxnRef; // Thông tin đơn hàng

                String paymentUrl = vnPayService.createPaymentUrl(vnp_Amount, vnp_OrderInfo, vnp_TxnRef, vnp_IpAddr);

                log.info("💳 URL thanh toán VNPay: {}", paymentUrl);
                return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paymentUrl));
            }

            CreateOrderResponse createOrderResponse = CreateOrderResponse.fromOrder(savedOrder);
            log.info("✅ Đơn hàng được tạo thành công: {}", createOrderResponse);

            return ResponseEntity.ok(createOrderResponse);

        } catch (Exception e) {
            log.error("🔥 Lỗi khi tạo đơn hàng: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Có lỗi xảy ra khi tạo đơn hàng, vui lòng thử lại sau.",
                            "order",
                            null,
                            e.getMessage()
                    ));
        }


    }
    public void updateOrderStatus(Long orderId, String paymentStatus) {
        // Tìm đơn hàng theo orderId
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // Tìm trạng thái mới dựa trên paymentStatus
            String statusName = "FAILURE"; // Mặc định là FAILURE
            if ("SUCCESS".equals(paymentStatus)) {
                statusName = "DONE"; // Cập nhật trạng thái khi thanh toán thành công
            }

            // Tìm OrderStatus trong DB theo statusName
            Optional<OrderStatus> orderStatusOptional = orderStatusRepository.findByStatusName(statusName);
            if (orderStatusOptional.isPresent()) {
                OrderStatus orderStatus = orderStatusOptional.get();
                // Cập nhật trạng thái đơn hàng
                order.setOrderStatus(OrderStatus.fromOrderStatus(orderStatus));
            } else {
                throw new RuntimeException("OrderStatus not found for status: " + statusName);
            }

            // Lưu lại trạng thái đã thay đổi vào cơ sở dữ liệu
            orderRepository.save(order);
        } else {
            // Xử lý nếu không tìm thấy đơn hàng
            throw new RuntimeException("Order not found for ID: " + orderId);
        }
    }

    public PreviewOrderResponse previewOrder(PreviewOrderRequest request) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/preview";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ShopId", String.valueOf(195952));
        headers.set("Token", ghnConfig.getToken());
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Content-Type", "application/json");

        HttpEntity<PreviewOrderRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<GhnPreviewResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, GhnPreviewResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return PreviewOrderResponse.fromGHNResponse(response.getBody());
            } else {
                throw new RuntimeException("Lấy thông tin preview đơn hàng thất bại! Mã lỗi: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            System.out.println("GHN Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi từ GHN: " + e.getMessage());
        }

    }

//    public GhnCreateOrderResponse createOrder(GhnCreateOrderRequest request) {
//        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Token", ghnConfig.getToken());
//        headers.set("ShopId", String.valueOf(195952)); // Chắc chắn gửi đúng kiểu số nguyên
//        headers.set("User-Agent", "Mozilla/5.0");
//        headers.set("Accept", "application/json");
//
//        HttpEntity<GhnCreateOrderRequest> entity = new HttpEntity<>(request, headers);
//
//        // 🔹 Log request body để kiểm tra
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            String requestJson = objectMapper.writeValueAsString(request);
//
//            log.info("🚀 Request gửi lên GHN:");
//            log.info("URL: {}", url);
//            log.info("Headers: {}", headers);
//            log.info("Body: {}", requestJson);
//        } catch (JsonProcessingException e) {
//            log.error("Lỗi khi chuyển request thành JSON", e);
//        }
//
//
//        try {
//            ResponseEntity<GhnCreateOrderResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, GhnCreateOrderResponse.class);
//
//            log.info("🔥 Response từ GHN: {}", response);
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return response.getBody();
//            } else {
//                log.error("GHN trả về lỗi: {}", response.getStatusCode());
//                throw new RuntimeException("Tạo đơn hàng thất bại! Mã lỗi: " + response.getStatusCode());
//            }
//        } catch (HttpClientErrorException e) {
//            log.error("❌ GHN Response Error: {}", e.getResponseBodyAsString());
//            throw new RuntimeException("Lỗi từ GHN: " + e.getMessage());
//        }
//
//    }



}
