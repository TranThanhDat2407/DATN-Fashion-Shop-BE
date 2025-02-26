package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.config.GHNConfig;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.PreviewOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.PreviewOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.TotalOrderTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.CreateOrderResponse;

import com.example.DATN_Fashion_Shop_BE.dto.response.order.HistoryOrderResponse;

import com.example.DATN_Fashion_Shop_BE.dto.response.order.TotalOrderCancelTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.TotalRevenueTodayResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<?> createOrder(OrderRequest orderRequest, HttpServletRequest request) {
        log.info("🛒 Bắt đầu tạo đơn hàng cho userId: {}", orderRequest.getUserId());

        // 1️⃣ Lấy giỏ hàng của user
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

        // 2️⃣ Tính tổng tiền sản phẩm
        double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getProductVariant().getSalePrice() * item.getQuantity())
                .sum();

        // 3️⃣ Áp dụng mã giảm giá (nếu có)
        double discount = 0.0;
        Coupon coupon = null;
        if (orderRequest.getCouponId() != null) {
            coupon = couponRepository.findById(orderRequest.getCouponId())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ."));
            if (!coupon.getIsActive() || coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Mã giảm giá đã hết hạn hoặc không hợp lệ.");
            }
            discount = Math.min(coupon.getDiscountValue(), totalAmount);
        }

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

        // 5️⃣ Tính phí vận chuyển
        double shippingFee = ghnService.calculateShippingFee(address, cartItems);
        log.info("🚚 Phí vận chuyển: {}", shippingFee);
        // 6️⃣ Tính tổng tiền đơn hàng
        double finalAmount = totalAmount - discount + shippingFee;
        log.info("💰 Tổng tiền đơn hàng sau khi áp dụng mã giảm giá và phí vận chuyển: {}", finalAmount);

        ShippingMethod shippingMethod = shippingMethodRepository.findById(orderRequest.getShippingMethodId())
                .orElseThrow(() -> {
                    log.error("❌ Không tìm thấy phương thức vận chuyển hợp lệ với ID: {}", orderRequest.getShippingMethodId());
                    return new RuntimeException("Không tìm thấy phương thức vận chuyển hợp lệ.");
                });


        // 7️⃣ Xử lý thanh toán
        PaymentMethod paymentMethod = paymentMethodRepository.findById(orderRequest.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ."));

        // 🛒 Nếu là COD, tạo luôn đơn hàng
        if ("COD".equalsIgnoreCase(paymentMethod.getMethodName())) {
            return processCodOrder(orderRequest, cart, cartItems, coupon, finalAmount, fullShippingAddress, shippingFee,shippingMethod, paymentMethod);
        }

        // 💳 Nếu là VNPay, tạo đơn hàng trước khi tạo URL thanh toán
        if ("VNPAY".equalsIgnoreCase(paymentMethod.getMethodName())) {

            OrderStatus orderStatus = orderStatusRepository.findByStatusName("PENDING")
                    .orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không hợp lệ."));

            Order order = Order.builder()
                    .user(User.builder().id(orderRequest.getUserId()).build())
                    .coupon(coupon)
                    .totalAmount(finalAmount)
                    .orderStatus(orderStatus)
                    .shippingAddress(fullShippingAddress)
                    .shippingFee(shippingFee)
                    .shippingMethod(shippingMethod)
                    .taxAmount(0.0)
                    .transactionId(null)
                    .payments(new ArrayList<>())
                    .build();

            double totalPrice = finalAmount + shippingFee;
            order.setTotalPrice(totalPrice);


            Order savedOrder = orderRepository.save(order);
            log.info("✅ Đơn hàng VNPay đã được tạo với ID: {}", savedOrder.getId());

            List<OrderDetail> orderDetails = cartItems.stream().map(item ->
                    OrderDetail.builder()
                            .order(savedOrder)
                            .productVariant(item.getProductVariant())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getProductVariant().getSalePrice())
                            .totalPrice(item.getProductVariant().getSalePrice() * item.getQuantity())
                            .build()
            ).collect(Collectors.toList());

            orderDetailRepository.saveAll(orderDetails);
            log.info("✅ Đã lưu {} sản phẩm vào OrderDetail.", orderDetails.size());
            try {
                String vnp_TxnRef = String.valueOf(savedOrder.getId());
                long vnp_Amount = (long) (finalAmount * 100);
                String vnp_IpAddr = request.getRemoteAddr();
                String vnp_OrderInfo = "Thanh toan don hang " + vnp_TxnRef;

                String paymentUrl = vnPayService.createPaymentUrl(vnp_Amount, vnp_OrderInfo, vnp_TxnRef, vnp_IpAddr);

                log.info("💳 URL thanh toán VNPay: {}", paymentUrl);

                return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paymentUrl));
            }catch (Exception e) {
                log.error("❌ Lỗi khi tạo URL thanh toán VNPay: {}", e.getMessage());
                throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay.");
            }
        }

        throw new RuntimeException("Phương thức thanh toán không được hỗ trợ.");
    }




    // Xử lý đơn hàng khi thanh toán COD
    private ResponseEntity<?> processCodOrder(OrderRequest orderRequest, Cart cart, List<CartItem> cartItems,
                                              Coupon coupon, double finalAmount, String fullShippingAddress,
                                              double shippingFee,ShippingMethod shippingMethod, PaymentMethod paymentMethod) {
        OrderStatus orderStatus = orderStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không hợp lệ."));

        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalAmount(finalAmount)
                .orderStatus(orderStatus)
                .shippingAddress(fullShippingAddress)
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(0.0)
                .payments(new ArrayList<>())
                .build();

        double totalPrice = finalAmount + shippingFee;
        order.setTotalPrice(totalPrice);
        String vnp_TxnRef = String.valueOf(order.getId());
        order.setTransactionId(vnp_TxnRef);


        Order savedOrder = orderRepository.save(order);
        log.info("✅ Đơn hàng COD đã được tạo với ID: {}", savedOrder.getId());

        List<OrderDetail> orderDetails = cartItems.stream().map(item ->
                OrderDetail.builder()
                        .order(savedOrder)
                        .productVariant(item.getProductVariant())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getProductVariant().getSalePrice())
                        .totalPrice(item.getProductVariant().getSalePrice() * item.getQuantity())
                        .build()
        ).collect(Collectors.toList());

        orderDetailRepository.saveAll(orderDetails);
        log.info("✅ Đã lưu {} sản phẩm vào OrderDetail.", orderDetails.size());

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(paymentMethod)
                .paymentDate(new Date())
                .amount(finalAmount)
                .status("PENDING")
                .transactionCode(UUID.randomUUID().toString())
                .build();

        paymentRepository.save(payment);
        savedOrder.getPayments().add(payment);
        orderRepository.save(savedOrder);

        cartItemRepository.deleteAll(cartItems);

        log.info("✅ Giỏ hàng đã được xóa sau khi đặt hàng.");

        return ResponseEntity.ok(CreateOrderResponse.fromOrder(savedOrder));
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


    public Page<HistoryOrderResponse> getOrderHistoryByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);

        return ordersPage.map(HistoryOrderResponse::fromHistoryOrder);
    }


    public Page<HistoryOrderResponse> getOrdersByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByOrderStatus_StatusName(status, pageable);

        return orderPage.map(HistoryOrderResponse::fromHistoryOrder);
    }

    public Page<HistoryOrderResponse> getAllOrders(Pageable pageable) {

        Page<Order> ordersPage = orderRepository.findAll(pageable);

        return ordersPage.map(HistoryOrderResponse::fromHistoryOrder);
    }



    public TotalRevenueTodayResponse getTotalRevenueToday() {
        List<Order> totalRevenue = orderRepository.getTotalRevenueToday();
        TotalRevenueTodayResponse response = new TotalRevenueTodayResponse();
        Double total = 0.0;
        for (Order order : totalRevenue) {
            total += order.getTotalAmount();
        }
        response.setTotalRevenueToday(total);
        if (!totalRevenue.isEmpty()) {
            response.setRevenueTodayDate(totalRevenue.get(0).getCreatedAt());
        }

        return response;
    }

    public Double getTotalRevenueYesterday() {
        List<Order> totalRevenue = orderRepository.getTotalRevenueYesterday();
        Double total = 0.0;
       for (Order order : totalRevenue) {
           total += order.getTotalAmount();
       }

       return total;
    }

    public TotalOrderTodayResponse getTotalOrderToday() {
        List<Order> totalOrder = orderRepository.getTotalOrderCompleteToday();
        Integer count = totalOrder.size();
        TotalOrderTodayResponse response = new TotalOrderTodayResponse();

        response.setTotalOrder(count);
        if (!totalOrder.isEmpty()) {
            response.setRevenueTodayDate(totalOrder.get(0).getCreatedAt());
        }

        return response;
    }

    public Integer getTotalOrderYesterday() {
        List<Order> totalOrder = orderRepository.getTotalOrderYesterday();
        Integer count = totalOrder.size();

        return count;
    }

    public TotalOrderCancelTodayResponse getTotalOrderCancelToday() {
        List<Order> totalOrder = orderRepository.getTotalOrderCancelToday();
        Integer count = totalOrder.size();
        TotalOrderCancelTodayResponse response = new TotalOrderCancelTodayResponse();

        response.setTotalOrderCancel(count);
        if (!totalOrder.isEmpty()) {
            response.setOrderCancelDate(totalOrder.get(0).getCreatedAt());
        }

        return response;
    }
    public Integer getTotalOrderCancelYesterday() {
        List<Order> totalOrder = orderRepository.getTotalOrderCancelYesterday();
        Integer count = totalOrder.size();

        return count;
    }

}


