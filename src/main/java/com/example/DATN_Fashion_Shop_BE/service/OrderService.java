package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.config.GHNConfig;
import com.example.DATN_Fashion_Shop_BE.dto.request.CarItem.CarItemDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.GhnCreateOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.Item;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.PreviewOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod.ShippingMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnCreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.PreviewOrderResponse;

import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingOrderReviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
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
    private final UserAddressRepository userAddressRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final VnPayService vnPayService;
    private final GHNService ghnService;
    private final AddressRepository addressRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
//    @Transactional
//    public ResponseEntity<?> createOrder(OrderRequest orderRequest) {
//        // 🛒 1️⃣ Lấy giỏ hàng của user
//
//
//        Cart cart = cartRepository.findByUser_Id(orderRequest.getUserId())
//                .orElseThrow(() -> new RuntimeException(localizationUtils
//                        .getLocalizedMessage(MessageKeys.CART_NOT_FOUND, orderRequest.getUserId())));
//
//        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
//        if (cartItems.isEmpty()) {
//            throw new RuntimeException(localizationUtils
//                    .getLocalizedMessage(MessageKeys.CART_ITEM_NOT_FOUND, cart.getId()));
//        }
//
//        // 🏷️ 2️⃣ Tính tổng tiền sản phẩm
//        double totalAmount = cartItems.stream()
//                .mapToDouble(item -> item.getProductVariant().getSalePrice() * item.getQuantity())
//                .sum();
//
//        // 🎟️ 3️⃣ Áp dụng mã giảm giá (nếu có)
//        double discount = 0.0;
//        Coupon coupon = null;
//
//        if (orderRequest.getCouponId() != null) {
//            coupon = couponRepository.findById(orderRequest.getCouponId())
//                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ."));
//
//            if (!coupon.getIsActive() || coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
//                throw new RuntimeException("Mã giảm giá đã hết hạn hoặc không hợp lệ.");
//            }
//
//            discount = coupon.getDiscountValue();
//        }
//
//
//        discount = Math.min(discount, totalAmount);
//
//
//        // 📦 4️⃣ Chuẩn bị danh sách sản phẩm gửi GHN
//        List<Item> items = cartItems.stream()
//                .map(item -> Item.builder()
//                        .name(item.getProductVariant().getProduct()
//                                .getTranslations().stream()
//                                .filter(t -> t.getLanguage().equals("vi")) // Chọn ngôn ngữ tiếng Việt
//                                .findFirst()
//                                .map(ProductsTranslation::getName) // Lấy tên sản phẩm
//                                .orElse("Sản phẩm chưa có tên")) // Giá trị mặc định nếu không tìm thấy
//                        .quantity(item.getQuantity())
//                        .build())
//                .collect(Collectors.toList());
//
//        // 📍 5️⃣ Lấy địa chỉ giao hàng của user
//        UserAddress userAddress = userAddressRepository.findTopByUser_IdAndIsDefaultTrue(orderRequest.getUserId())
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ mặc định của người dùng."));
//
//        Address address = userAddress.getAddress(); // Lấy Address từ UserAddress
//        String fullShippingAddress = String.format("%s, %s, %s, %s",
//                address.getStreet(), address.getWard(), address.getDistrict(), address.getCity());
//
//        PreviewOrderRequest previewOrderRequest = PreviewOrderRequest.builder()
//                .payment_type_id(2)
//                .required_note("KHONGCHOXEMHANG")
//                .to_name(userAddress.getFirstName() + " " + userAddress.getLastName())
//                .to_phone(userAddress.getPhone())
//                .to_address(address.getStreet())
//                .to_ward_name(address.getWard())
//                .to_district_name(address.getDistrict())
//                .to_province_name(address.getCity())
//
//                // Thông tin kiện hàng
//                .length(40)
//                .width(30)
//                .height(20)
//                .weight(1000)
//                .service_type_id(2)
//                // Danh sách sản phẩm
//                .items(items)
//                .build();
//
//        double shippingFee;
//        try {
//            PreviewOrderResponse previewOrderResponse = previewOrder(previewOrderRequest);
//            shippingFee = previewOrderResponse.getShippingFee();
//            if (shippingFee <= 0) {
//                throw new RuntimeException("Phí vận chuyển không hợp lệ.");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi khi lấy phí vận chuyển từ GHN: " + e.getMessage());
//        }
//
//
//        // 🛍 6️⃣ Tính tổng tiền đơn hàng
//        double finalAmount = totalAmount - discount + shippingFee;
//
//        ShippingMethod shippingMethod = shippingMethodRepository.findById(orderRequest.getShippingMethodId())
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức vận chuyển hợp lệ."));
//
//        // 📦 7️⃣ Tạo Order
//        Order order = Order.builder()
//
//                .user(User.builder().id(orderRequest.getUserId()).build())
//                .coupon(coupon)
//                .totalPrice(totalAmount)
//                .totalAmount(finalAmount)
//                .orderStatus(OrderStatus.builder().id(1L).build())
//                .shippingAddress(fullShippingAddress) // Lưu địa chỉ dạng String
//                .shippingFee(shippingFee)
//                .shippingMethod(shippingMethod)
//                .taxAmount(0.0)
//                .build();
//
//        Order savedOrder = orderRepository.save(order);
//
//        // 🛒 8️⃣ Lưu OrderDetail
//        List<OrderDetail> orderDetails = cartItems.stream().map(item ->
//                OrderDetail.builder()
//                        .order(savedOrder)
//                        .product(item.getProductVariant().getProduct())
//                        .quantity(item.getQuantity())
//                        .unitPrice(item.getProductVariant().getSalePrice())
//                        .totalPrice(item.getProductVariant().getSalePrice() * item.getQuantity())
//                        .build()).collect(Collectors.toList());
//
//        orderDetailRepository.saveAll(orderDetails);
//
//        // 💳 9️⃣ Xử lý thanh toán
//        PaymentMethod paymentMethod = paymentMethodRepository.findById(orderRequest.getPaymentMethodId())
//                .orElseThrow(() -> new RuntimeException(localizationUtils
//                        .getLocalizedMessage(MessageKeys.PAYMENT_METHOD_NOT_VALID)));
//
//        Payment payment = Payment.builder()
//                .order(savedOrder)
//                .paymentMethod(paymentMethod)
//                .paymentDate(new Date())
//                .amount(finalAmount)
//                .status("PENDING")
//                .transactionCode(UUID.randomUUID().toString())
//                .build();
//
//
//        paymentRepository.save(payment);
//        if ("COD".equalsIgnoreCase(paymentMethod.getMethodName())) {
//            // 🚚 Thanh toán khi nhận hàng -> Không cần xử lý gì thêm
//            log.info("Đơn hàng {} sẽ thanh toán khi nhận hàng (COD).", savedOrder.getId());
//        } else if ("VNPAY".equalsIgnoreCase(paymentMethod.getMethodName())) {
//            // 🌐 Xử lý thanh toán VNPay
//            String vnpayUrl = vnPayService.createPaymentUrl(savedOrder);
//            log.info("URL thanh toán VNPay cho đơn hàng {}: {}", savedOrder.getId(), vnpayUrl);
//
//            // Trả về URL để frontend redirect người dùng đến trang thanh toán VNPay
//            return ResponseEntity.ok(Collections.singletonMap("paymentUrl", vnpayUrl));
//        }
//        // 🛒 🔟 Xóa giỏ hàng sau khi đặt hàng thành công
//        cartItemRepository.deleteAll(cartItems);
//        cartRepository.delete(cart);
//
//        return ResponseEntity.ok(ApiResponseUtils.successResponse(
//                localizationUtils.getLocalizedMessage(MessageKeys.ORDERS_SUCCESSFULLY),
//                savedOrder
//        ));
//    }

    @Transactional
    public ResponseEntity<?> createOrder(OrderRequest orderRequest) {
        // 🛒 1️⃣ Lấy giỏ hàng của user
        Cart cart = cartRepository.findByUser_Id(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException(localizationUtils
                        .getLocalizedMessage(MessageKeys.CART_NOT_FOUND, orderRequest.getUserId())));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException(localizationUtils
                    .getLocalizedMessage(MessageKeys.CART_ITEM_NOT_FOUND, cart.getId()));
        }

        // 🏷️ 2️⃣ Tính tổng tiền sản phẩm
        double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getProductVariant().getSalePrice() * item.getQuantity())
                .sum();

        // 🎟️ 3️⃣ Áp dụng mã giảm giá (nếu có)
        double discount = 0.0;
        Coupon coupon = null;

        if (orderRequest.getCouponId() != null) {
            coupon = couponRepository.findById(orderRequest.getCouponId())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ."));

            if (!coupon.getIsActive() || coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Mã giảm giá đã hết hạn hoặc không hợp lệ.");
            }

            discount = coupon.getDiscountValue();
        }
        discount = Math.min(discount, totalAmount);

        // 📍 5️⃣ Xử lý địa chỉ giao hàng
        Address address;
        if (orderRequest.getShippingAddress() != null) {
            address = addressRepository.findById(orderRequest.getShippingAddress())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ được chọn."));
        } else {
            UserAddress userAddress = userAddressRepository.findTopByUser_IdAndIsDefaultTrue(orderRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ mặc định của người dùng."));
            address = userAddress.getAddress();
        }

        String fullShippingAddress = String.format("%s, %s, %s, %s",
                address.getStreet(), address.getWard(), address.getDistrict(), address.getCity());
        // 🚚 6️⃣ Tính phí vận chuyển
        double shippingFee = ghnService.calculateShippingFee(address, cartItems);

        // 🛍 7️⃣ Tính tổng tiền đơn hàng
        double finalAmount = totalAmount - discount + shippingFee;

        ShippingMethod shippingMethod = shippingMethodRepository.findById(orderRequest.getShippingMethodId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức vận chuyển hợp lệ."));

        // 📦 8️⃣ Tạo Order
        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalPrice(totalAmount)
                .totalAmount(finalAmount)
                .orderStatus(OrderStatus.builder().id(1L).build())
                .shippingAddress(fullShippingAddress)
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(0.0)
                .build();

        System.out.println("Shipping Fee Calculated: {}" +shippingFee);

        Order savedOrder = orderRepository.save(order);

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

        // 💳 🔟 Xử lý thanh toán
        PaymentMethod paymentMethod = paymentMethodRepository.findById(orderRequest.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException(localizationUtils
                        .getLocalizedMessage(MessageKeys.PAYMENT_METHOD_NOT_VALID)));

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(paymentMethod)
                .paymentDate(new Date())
                .amount(finalAmount)
                .status("PENDING")
                .transactionCode(UUID.randomUUID().toString())
                .build();

        paymentRepository.save(payment);

        if ("COD".equalsIgnoreCase(paymentMethod.getMethodName())) {
            log.info("Đơn hàng {} sẽ thanh toán khi nhận hàng (COD).", savedOrder.getId());
        } else if ("VNPAY".equalsIgnoreCase(paymentMethod.getMethodName())) {
            // Xử lý VNPay
            String vnpayUrl = vnPayService.createPaymentUrl(savedOrder, payment);
            log.info("URL thanh toán VNPay cho đơn hàng {}: {}", savedOrder.getId(), vnpayUrl);

            return ResponseEntity.ok(Collections.singletonMap("paymentUrl", vnpayUrl));
        }

        // 🛒 🔟 Xóa giỏ hàng sau khi đặt hàng thành công
        cartItemRepository.deleteAll(cartItems);
        cartRepository.delete(cart);

        return ResponseEntity.ok(Collections.singletonMap("orderId", savedOrder.getId()));
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
