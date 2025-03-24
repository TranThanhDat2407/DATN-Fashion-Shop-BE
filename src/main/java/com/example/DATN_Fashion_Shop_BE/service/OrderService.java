package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.config.GHNConfig;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.PreviewOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.Notification.NotificationTranslationRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.inventory_transfer.InventoryTransferItemRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.inventory_transfer.InventoryTransferRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.ClickAndCollectOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.UpdateStoreOrderStatusRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.UpdateStorePaymentMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.store.StorePaymentRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.PreviewOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.TotalOrderTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.*;

import com.example.DATN_Fashion_Shop_BE.dto.response.order.HistoryOrderResponse;

import com.example.DATN_Fashion_Shop_BE.dto.response.order.TotalOrderCancelTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.TotalRevenueTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StorePaymentResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
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
    private final CartService cartService;
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
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final InventoryTransferService inventoryTransferService;
    private final CouponUserRestrictionRepository couponUserRestrictionRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final AddressService addressService;


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
    @Transactional
    public ResponseEntity<?> processCodOrder(OrderRequest orderRequest, Cart cart, List<CartItem> cartItems,
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


        Product product = orderDetails.getFirst().getProductVariant().getProduct();
        ProductVariant variant = orderDetails.getFirst().getProductVariant();
        AttributeValue color = variant.getColorValue();
        String productImage = null;
        if (product.getMedias() != null && !product.getMedias().isEmpty()) {
            productImage = product.getMedias().stream()
                    .filter(media -> media.getColorValue() != null && color != null && media.getColorValue().getId().equals(color.getId())) // So sánh bằng ID thay vì equals()
                    .map(ProductMedia::getMediaUrl)
                    .findFirst()
                    .orElse(product.getMedias().get(0).getMediaUrl()); // Nếu không có, lấy ảnh đầu tiên
        }

        List<NotificationTranslationRequest> translations = List.of(
                new NotificationTranslationRequest("vi", "Trạng thái đơn hàng", notificationService.getVietnameseMessage(savedOrder.getId(), orderStatus)),
                new NotificationTranslationRequest("en", "Order Status", notificationService.getEnglishMessage(savedOrder.getId(), orderStatus)),
                new NotificationTranslationRequest("jp", "注文状況", notificationService.getJapaneseMessage(savedOrder.getId(), orderStatus))
        );

        // Gọi createNotification()
        notificationService.createNotification(
                orderRequest.getUserId(),
                "ORDER",
                ""+savedOrder.getId(), // redirectUrl không cần backend xử lý
                productImage, // imageUrl không cần backend xử lý
                translations
        );

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

        User userWithAddresses = userRepository.findById(savedOrder.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        log.info("📌 User Addresses từ DB: {}", userWithAddresses.getUserAddresses());

        List<UserAddressResponse> userAddressResponses = (userWithAddresses.getUserAddresses() != null)
                ? userWithAddresses.getUserAddresses().stream()
                .map(UserAddressResponse::fromUserAddress)
                .collect(Collectors.toList())
                : new ArrayList<>();

        log.info("📌 userAddressResponses: {}", userAddressResponses);


        // Sau khi lưu OrderDetail, lấy lại đơn hàng từ DB để cập nhật danh sách OrderDetail
        Order reloadedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng đã lưu!"));

        // Đảm bảo OrderDetails không bị null
        if (reloadedOrder.getOrderDetails() == null) {
            reloadedOrder.setOrderDetails(new ArrayList<>());
        }

        // Truy vấn lại danh sách OrderDetail từ DB
        List<OrderDetail> reloadedOrderDetails = orderDetailRepository.findByOrderId(savedOrder.getId());

        List<OrderDetailResponse> orderDetailResponses = reloadedOrderDetails.stream()
                .map(orderDetail -> OrderDetailResponse.fromOrderDetail(orderDetail, userAddressResponses))
                .collect(Collectors.toList());


        log.info("📌 userAddressResponses: {}", userAddressResponses);

        // ✅ Gửi email xác nhận đơn hàng
        if (userWithAddresses.getEmail() != null && !userWithAddresses.getEmail().isEmpty()) {
            emailService.sendOrderConfirmationEmail(userWithAddresses.getEmail(), orderDetailResponses);
            log.info("📧 Đã gửi email xác nhận đơn hàng đến {}", userWithAddresses.getEmail());
        } else {
            log.warn("⚠ Không thể gửi email xác nhận đơn hàng vì email của người dùng không tồn tại.");
        }


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

    @Transactional
    public StorePaymentResponse createStoreOrder(Long staffId, StorePaymentRequest request)
            throws DataNotFoundException {
        // Kiểm tra nhân viên có tồn tại không
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new DataNotFoundException("Staff not found with ID: " + staffId));

        // Nếu có userId, lấy User từ DB, nếu không thì để null
        User user = (request.getUserId() != null) ?
                userRepository.findById(request.getUserId()).orElse(null) : null;

        // Lấy giỏ hàng của nhân viên (staffId)
        Cart cart = cartRepository.findByUser_Id(staffId)
                .orElseThrow(() -> new DataNotFoundException("Cart not found for Staff ID: " + staffId));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new DataNotFoundException("store not found for Staff ID: " + (request.getStoreId())));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty, cannot create order.");
        }

        // Lấy Coupon nếu có
        Coupon coupon = (request.getCouponId() != null) ?
                couponRepository.findById(request.getCouponId()).orElse(null) : null;

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new DataNotFoundException("Payment method not found"));

        // Tạo đơn hàng mới
        Order order = Order.builder()
                .user(user)
                .store(store)
                .coupon(coupon)
                .totalPrice(request.getTotalPrice())
                .totalAmount(request.getTotalAmount())
                .shippingFee(0D)
                .taxAmount(request.getTaxAmount())
                .shippingAddress(store.getAddress().getFullAddress())
                .orderStatus(orderStatusRepository.findByStatusName("DONE").orElseThrow(null))
                .build();

        // Lưu đơn hàng
        order = orderRepository.save(order);

        // Thêm các sản phẩm vào OrderDetail
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .productVariant(cartItem.getProductVariant())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getProductVariant().getAdjustedPrice())
                    .totalPrice(cartItem.getProductVariant().getAdjustedPrice() * cartItem.getQuantity())
                    .build();
            orderDetails.add(orderDetail);
        }
        orderDetailRepository.saveAll(orderDetails);

        // Lưu thanh toán
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentDate(new Date())
                .amount(order.getTotalPrice())
                .status("COMPLETED") // Giả sử thanh toán tại cửa hàng luôn hoàn tất
                .transactionCode(request.getTransactionCode() != null ?
                        request.getTransactionCode() : UUID.randomUUID().toString())
                .build();
        paymentRepository.save(payment);

        for (OrderDetail orderDetail : orderDetails) {
            Inventory inventory = inventoryRepository
                    .findByStoreIdAndProductVariantId(store.getId(), orderDetail.getProductVariant().getId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Inventory not found for Product Variant ID: " + orderDetail.getProductVariant().getId()
                                    + " in Store ID: " + store.getId()));

            if (inventory.getQuantityInStock() < orderDetail.getQuantity()) {
                throw new IllegalStateException("Not enough stock available for Product Variant ID: "
                        + orderDetail.getProductVariant().getId());
            }

            inventory.setQuantityInStock(inventory.getQuantityInStock() - orderDetail.getQuantity());
            inventoryRepository.save(inventory);
        }

        if(user != null && coupon != null) {
        couponUserRestrictionRepository.deleteByCouponIdAndUserId(user.getId(), coupon.getId());
        }

        return StorePaymentResponse.fromOrder(order);
    }

    public Page<StoreOrderResponse> getStoreOrdersByFilters(
            Long storeId,
            Long orderStatusId,
            Long paymentMethodId,
            Long shippingMethodId,
            Long customerId,
            Long staffId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String languageCode,
            Pageable pageable
    ) {
        Page<Order> orders = orderRepository.findOrdersByFilters(
                storeId, orderStatusId, paymentMethodId, shippingMethodId, customerId, staffId, startDate, endDate, pageable
        );

        return orders.map(item -> StoreOrderResponse.fromOrder(item, languageCode));
    }

    public List<StoreOrderResponse> getStoreOrdersByFilters(
            Long storeId,
            Long orderStatusId,
            Long paymentMethodId,
            Long shippingMethodId,
            Long customerId,
            Long staffId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String languageCode
    ) {
        List<Order> orders = orderRepository.findOrdersByFilters(
                storeId, orderStatusId, paymentMethodId, shippingMethodId, customerId, staffId, startDate, endDate
        );

        return orders.stream()
                .map(order -> StoreOrderResponse.fromOrder(order, languageCode))
                .collect(Collectors.toList());
    }

    public StoreOrderResponse getStoreOrderById(Long orderId, String languageCode)
            throws DataNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        return StoreOrderResponse.fromOrder(order, languageCode);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, UpdateStoreOrderStatusRequest request) throws DataNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        if ("DONE".equals(order.getOrderStatus().getStatusName())) {
            throw new IllegalStateException("Cannot update a completed order.");
        }


        OrderStatus newStatus = orderStatusRepository.findByStatusName(request.getStatusName())
                .orElseThrow(() -> new DataNotFoundException("Order status not found"));

        if(newStatus.getStatusName().equals("READY-TO-PICKUP")){
            emailService.sendOrderReadyForPickupEmail(
                    order.getUser().getEmail(),
                    StoreOrderResponse.fromOrder(order,"vi")
            );
        }

        if(newStatus.getStatusName().equals("DONE")){
            emailService.sendPaymentSuccessEmail(
                    order.getUser().getEmail(),
                    StoreOrderResponse.fromOrder(order,"vi")
            );
            
                for (OrderDetail detail : order.getOrderDetails()) {
                    inventoryService.reduceInventory(
                            detail.getProductVariant().getId(),
                            detail.getQuantity(),
                            order.getStore().getId()
                    );
                }

        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }

    // Cập nhật phương thức thanh toán
    @Transactional
    public void updatePaymentMethod(Long orderId, UpdateStorePaymentMethodRequest request) throws DataNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        PaymentMethod paymentMethod = paymentMethodRepository.findByMethodName(request.getPaymentMethodName())
                .orElseThrow(() -> new DataNotFoundException("Payment method not found"));

        // Giả sử đơn hàng chỉ có một payment, cập nhật nó
        if (!order.getPayments().isEmpty()) {
            Payment payment = order.getPayments().get(0);
            payment.setPaymentMethod(paymentMethod);
            payment.setStatus("PAID");
        } else {
            throw new IllegalStateException("No payment record found for this order.");
        }

        orderRepository.save(order);
    }


    @Transactional
    public ResponseEntity<?> createClickAndCollectOrder(ClickAndCollectOrderRequest orderRequest, HttpServletRequest request) {
        log.info("🛒 Bắt đầu tạo đơn hàng Click & Collect cho userId: {}", orderRequest.getUserId());

        // 1️⃣ Lấy giỏ hàng của user
        Cart cart = cartRepository.findByUser_Id(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.CART_NOT_FOUND, orderRequest.getUserId())));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.CART_ITEM_NOT_FOUND, cart.getId()));
        }

        // 2️⃣ Kiểm tra tồn kho của Store trước khi tạo đơn
        List<InventoryTransferItemRequest> transferItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            ProductVariant productVariant = item.getProductVariant();
            int requestedQuantity = item.getQuantity();

            // Lấy số lượng hàng tồn kho tại Store
            int storeStock = inventoryRepository.findByProductVariantIdAndStoreNotNull(productVariant.getId())
                    .stream().mapToInt(Inventory::getQuantityInStock).sum();

            if (storeStock < requestedQuantity) {
                log.warn("⚠️ Sản phẩm {} không đủ hàng tại Store, cần chuyển từ Warehouse", productVariant.getId());
                transferItems.add(new InventoryTransferItemRequest(productVariant.getId(), requestedQuantity - storeStock));
            }
        }


        double totalAmount = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getProductVariant().getSalePrice() * item.getQuantity())
                .sum();

        // 5️⃣ Áp dụng mã giảm giá (nếu có)
        double discount = 0.0;
        Coupon coupon = null;
        if (orderRequest.getCouponId() != null) {
            coupon = couponRepository.findById(orderRequest.getCouponId())
                    .filter(Coupon::getIsActive)
                    .filter(c -> c.getExpirationDate().isAfter(LocalDateTime.now()))
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ hoặc đã hết hạn."));

            discount = Math.min(coupon.getDiscountValue(), totalPrice);
        }

        // 6️⃣ Lấy thông tin Store và địa chỉ
        Store store = storeRepository.findById(orderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng với ID: " + orderRequest.getStoreId()));

        Address storeAddress = Optional.ofNullable(store.getAddress())
                .orElseThrow(() -> new RuntimeException("Cửa hàng không có địa chỉ hợp lệ."));

        String fullStoreAddress = String.format("%s, %s, %s, %s",
                storeAddress.getStreet(), storeAddress.getWard(), storeAddress.getDistrict(), storeAddress.getCity());

        log.info("📍 Địa chỉ cửa hàng: {}", fullStoreAddress);

        // 7️⃣ Phí vận chuyển = 0 vì khách nhận hàng tại cửa hàng
        double shippingFee = 0.0;
        double finalAmount = totalPrice - discount + totalPrice * 0.1;

        log.info("💰 Tổng tiền đơn hàng sau khi áp dụng mã giảm giá: {}", finalAmount);

        // 8️⃣ Xử lý thanh toán
        PaymentMethod paymentMethod = paymentMethodRepository.findById(orderRequest.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ."));

        OrderStatus orderStatus = orderStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không hợp lệ."));

        ShippingMethod shippingMethod = shippingMethodRepository.findById(2L)
                .orElseThrow(() -> {
                    return new RuntimeException("Không tìm thấy phương thức vận chuyển hợp lệ.");
                });

        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User với ID: " + orderRequest.getUserId()));


        // ✅ Tạo đơn hàng
        Order order = Order.builder()
                .user(user)
                .coupon(coupon)
                .totalAmount(totalAmount)
                .totalPrice(finalAmount)
                .orderStatus(orderStatus)
                .shippingAddress(store.getAddress().getFullAddress())
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(totalPrice * 0.1)
                .transactionId(null)
                .store(store)
                .payments(new ArrayList<>())
                .build();

        order.setTotalPrice(finalAmount + shippingFee);
        Order savedOrder = orderRepository.save(order);
        log.info("✅ Đơn hàng đã được tạo với ID: {}", savedOrder.getId());

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(paymentMethod)
                .amount(finalAmount)
                .paymentDate(new Date())
                .status("UNPAID")
                .transactionCode("")
                .build();

        paymentRepository.save(payment);

        savedOrder.getPayments().add(payment);
        orderRepository.save(savedOrder);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem item : cartItems) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .order(savedOrder)
                    .productVariant(item.getProductVariant())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getProductVariant().getAdjustedPrice())
                    .totalPrice(item.getProductVariant().getAdjustedPrice() * item.getQuantity())
                    .build();

            orderDetails.add(orderDetail);
        }

        orderDetailRepository.saveAll(orderDetails);

        savedOrder.setOrderDetails(orderDetails);
        orderRepository.save(savedOrder);

        log.info("email address: {}", savedOrder.getUser().getEmail());

        emailService.sendOrderConfirmationEmail(
                savedOrder.getUser().getEmail(),
                StoreOrderResponse.fromOrder(savedOrder,"vi")
        );

        cartService.clearCart(savedOrder.getUser().getId(),"");

        if (!transferItems.isEmpty()) {
            log.info("📦 Cần chuyển hàng từ Warehouse về Store trước khi tạo đơn");

            InventoryTransferRequest transferRequest = InventoryTransferRequest.builder()
                    .warehouseId(1L)
                    .storeId(orderRequest.getStoreId())         // Store nhận hàng
                    .transferItems(transferItems)
                    .message("for order id #" + savedOrder.getId())
                    .build();

            InventoryTransfer transfer = inventoryTransferService.createTransfer(transferRequest);
            log.info("✅ Đã tạo yêu cầu chuyển kho với ID: {}", transfer.getId());

            return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict
                    .body(Collections.singletonMap("message", "Sản phẩm không đủ hàng tại Store. Đã tạo yêu cầu chuyển kho #" + transfer.getId()));
        }

        // Nếu thanh toán tại cửa hàng, trả về ID đơn hàng
        if ("Pay-in-store".equalsIgnoreCase(paymentMethod.getMethodName())) {
            return ResponseEntity.ok(Collections.singletonMap("orderId", savedOrder.getId()));
        }

        // Nếu thanh toán qua VNPay, tạo URL thanh toán
        if ("VNPAY".equalsIgnoreCase(paymentMethod.getMethodName())) {
            try {
                String vnp_TxnRef = String.valueOf(savedOrder.getId());
                long vnp_Amount = (long) (finalAmount * 100);
                String vnp_IpAddr = request.getRemoteAddr();
                String vnp_OrderInfo = "Thanh toán đơn hàng " + vnp_TxnRef;

                String paymentUrl = vnPayService.createPaymentUrl(vnp_Amount, vnp_OrderInfo, vnp_TxnRef, vnp_IpAddr);
                log.info("💳 URL thanh toán VNPay: {}", paymentUrl);

                payment.setStatus("PAID");
                paymentRepository.save(payment);
                return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paymentUrl));
            } catch (Exception e) {
                log.error("❌ Lỗi khi tạo URL thanh toán VNPay: {}", e.getMessage());
                throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay.");
            }
        }

        throw new RuntimeException("Phương thức thanh toán không được hỗ trợ.");
    }

}


