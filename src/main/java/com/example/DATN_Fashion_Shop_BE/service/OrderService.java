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
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top3Store;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StorePaymentResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.exception.BadRequestException;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.exception.NotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
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
    private final MomoService momoService;
    private final MomoStoreService momoStoreService;
    private final CurrencyService currencyService;
    private final AddressService addressService;
    private final PaypalService paypalService;

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


        // 5️⃣ Tính phí vận chuyển

        double shippingFee = ghnService.calculateShippingFee(address, cartItems);
        log.info("🚚 Phí vận chuyển: {}", shippingFee);
        // 6️⃣ Tính tổng tiền đơn hàng

        double subtotal = totalAmount - discount ; // tổng tiền trước thuế
        double tax = subtotal * 0.08;
        double grandTotal = Math.round((subtotal + tax + shippingFee) * 100) / 100.0; // tổng tiền sau thuế

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
            return processCodOrder(
                    orderRequest, cart,
                    cartItems, coupon,
                    subtotal, fullShippingAddress,
                    shippingFee, shippingMethod,
                    paymentMethod);
        }
        if ("PAY-IN-STORE".equalsIgnoreCase(paymentMethod.getMethodName())) {
            if (orderRequest.getStoreId() == null) {
                throw new RuntimeException("Vui lòng chọn cửa hàng nhận hàng");
            }

            ClickAndCollectOrderRequest clickAndCollectRequest = ClickAndCollectOrderRequest.builder()
                    .userId(orderRequest.getUserId())
                    .couponId(orderRequest.getCouponId())
                    .paymentMethodId(orderRequest.getPaymentMethodId())
                    .storeId(orderRequest.getStoreId())
                    .build();

            return createClickAndCollectOrder(clickAndCollectRequest, request);
        }

        // 💳 Nếu là VNPay, tạo đơn hàng trước khi tạo URL thanh toán
        if ("VNPAY".equalsIgnoreCase(paymentMethod.getMethodName())) {
            return processVnPayPayment(orderRequest, request, cartItems, coupon, subtotal,
                    fullShippingAddress, shippingFee, shippingMethod, grandTotal);
        }

        // 📱 Nếu là MoMo tạo đơn hàng trước khi tạo URL thanh toán
        if ("MOMO".equalsIgnoreCase(paymentMethod.getMethodName())) {
            return processMoMoPayment(orderRequest, request, cartItems, coupon, subtotal,
                    fullShippingAddress, shippingFee, shippingMethod, grandTotal);
        }
        if ("PAYPAL".equalsIgnoreCase(paymentMethod.getMethodName())) {
            double grandTotalInUsd = currencyService.convertFromVnd(1406501, "USD");
            return processPayPalPayment(orderRequest, request, cartItems, coupon, subtotal,
                    fullShippingAddress, shippingFee, shippingMethod, grandTotalInUsd);
        }

        throw new RuntimeException("Phương thức thanh toán không được hỗ trợ.");
    }

    // Xử lý đơn hàng khi thanh toán COD
    @Transactional
    public ResponseEntity<?> processCodOrder(OrderRequest orderRequest, Cart cart, List<CartItem> cartItems,
                                             Coupon coupon, double subtotal, String fullShippingAddress,
                                             double shippingFee,ShippingMethod shippingMethod, PaymentMethod paymentMethod) {
        OrderStatus orderStatus = orderStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không hợp lệ."));

        double tax = Math.round((subtotal * 0.08) * 100.0) / 100.0;
        double grandTotal = Math.round((subtotal + tax + shippingFee) * 100) / 100.0;
        if (subtotal < 0) {
            throw new RuntimeException("Tổng tiền không hợp lệ");
        }

        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalAmount(subtotal) // tổng trước thuế (totalAmount - discount)
                .totalPrice(grandTotal) // tổng sau thuế (+tax +shipping)
                .orderStatus(orderStatus)
                .shippingAddress(fullShippingAddress)
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(tax)
                .payments(new ArrayList<>())
                .build();



        Order savedOrder = orderRepository.save(order);
        String vnp_TxnRef = String.valueOf(order.getId());
        order.setTransactionId(vnp_TxnRef);



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
                .amount(grandTotal)
                .status("UNPAID")
                .transactionCode(UUID.randomUUID().toString())
                .build();

        paymentRepository.save(payment);
        savedOrder.getPayments().add(payment);
        orderRepository.save(savedOrder);

        User userWithAddresses = userRepository.findById(savedOrder.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));



        List<UserAddressResponse> userAddressResponses = (userWithAddresses.getUserAddresses() != null)
                ? userWithAddresses.getUserAddresses().stream()
                .map(UserAddressResponse::fromUserAddress)
                .collect(Collectors.toList())
                : new ArrayList<>();




        // Sau khi lưu OrderDetail, lấy lại đơn hàng từ DB để cập nhật danh sách OrderDetail
        Order reloadedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng đã lưu!"));

        // Đảm bảo OrderDetails không bị null
        if (reloadedOrder.getOrderDetails() == null) {
            reloadedOrder.setOrderDetails(new ArrayList<>());
        }

        // Truy vấn lại danh sách OrderDetail từ DB
        List<OrderDetail> reloadedOrderDetails = orderDetailRepository.findByOrderId(savedOrder.getId());

        List<OrderDetailResponse> orderDetailResponses = orderDetails.stream()
                .map(orderDetail -> OrderDetailResponse.fromOrderDetail(orderDetail, userAddressResponses, paymentRepository))
                .collect(Collectors.toList());



        subtractInventoryForOrder(reloadedOrder);
        // ✅ Gửi email xác nhận đơn hàng
        if (userWithAddresses.getEmail() != null && !userWithAddresses.getEmail().isEmpty()) {
            emailService.sendOrderConfirmationEmail(userWithAddresses.getEmail(), orderDetailResponses);
//            emailProducer.sendOrderEmail(userWithAddresses.getEmail(), orderDetailResponses);
            log.info("📧 Đã gửi email xác nhận đơn hàng đến {}", userWithAddresses.getEmail());
        } else {
            log.warn("⚠ Không thể gửi email xác nhận đơn hàng vì email của người dùng không tồn tại.");
        }


        cartItemRepository.deleteAll(cartItems);

        log.info("✅ Giỏ hàng đã được xóa sau khi đặt hàng.");

        return ResponseEntity.ok(CreateOrderResponse.fromOrder(savedOrder));
    }


    private ResponseEntity<?> processVnPayPayment(OrderRequest orderRequest, HttpServletRequest request,
                                                  List<CartItem> cartItems, Coupon coupon, double subtotal, String fullShippingAddress,
                                                  double shippingFee, ShippingMethod shippingMethod, double grandTotal) {

        OrderStatus orderStatus = orderStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không hợp lệ."));

        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalAmount(subtotal) // tổng trước thuế (totalAmount - discount)
                .totalPrice(grandTotal) // tổng sau thuế (+tax +shipping)
                .orderStatus(orderStatus)
                .shippingAddress(fullShippingAddress)
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(subtotal * 0.08)
                .transactionId(null)
                .payments(new ArrayList<>())
                .build();


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


        try {
            String vnp_TxnRef = String.valueOf(savedOrder.getId());
            long vnp_Amount = (long) (grandTotal * 100);
            String vnp_IpAddr = request.getRemoteAddr();
            String vnp_OrderInfo = "Thanh toan don hang " + vnp_TxnRef;

            String paymentUrl = VNPayService.createPaymentUrl(vnp_Amount, vnp_OrderInfo, vnp_TxnRef, vnp_IpAddr);
            subtractInventoryForOrder(savedOrder);
            log.info("💳 URL thanh toán VNPay: {}", paymentUrl);

            return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paymentUrl));
        }catch (Exception e) {
            log.error("❌ Lỗi khi tạo URL thanh toán VNPay: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay.");
        }

    }

    private ResponseEntity<?> processMoMoPayment(OrderRequest orderRequest, HttpServletRequest request,
                                                 List<CartItem> cartItems, Coupon coupon, double subtotal, String fullShippingAddress,
                                                 double shippingFee, ShippingMethod shippingMethod, double grandTotal) {

        // 1. Tạo đơn hàng với trạng thái PENDING
        OrderStatus orderStatus = orderStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không hợp lệ."));

        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalAmount(subtotal)
                .totalPrice(grandTotal)
                .orderStatus(orderStatus)
                .shippingAddress(fullShippingAddress)
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(subtotal * 0.08)
                .transactionId(null)
                .payments(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("✅ Đơn hàng MoMo đã được tạo với ID: {}", savedOrder.getId());

        // 2. Lưu order details
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
            // 3. Tạo yêu cầu thanh toán MoMo
            String orderId = String.valueOf(savedOrder.getId());
            String orderInfo = "Thanh toán đơn hàng " + orderId;
            long amount = Math.round(grandTotal);

            Map<String, Object> momoResponse = momoService.createPayment(amount, orderInfo, orderId);

            String momoPaymentUrl = momoResponse.get("payUrl").toString();
            subtractInventoryForOrder(savedOrder);

            log.info("📱 URL  thanh toán MoMo: {}", momoPaymentUrl);
            return ResponseEntity.ok(Map.of(
                    "payUrl", momoPaymentUrl,
                    "ipnUrl", MomoService.IPN_URL,
                    "redirectUrl", MomoService.RETURN_URL,
                    "orderId", savedOrder.getId(),
                    "amount", grandTotal,
                    "orderInfo", "Thanh toán đơn hàng " + savedOrder.getId()
            ));


        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo yêu cầu thanh toán MoMo: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi tạo yêu cầu thanh toán MoMo.");
        }
    }

    private ResponseEntity<?> processPayPalPayment(OrderRequest orderRequest, HttpServletRequest request,
                                                 List<CartItem> cartItems, Coupon coupon, double subtotal, String fullShippingAddress,
                                                 double shippingFee, ShippingMethod shippingMethod, double grandTotal) {

        OrderStatus orderStatus = orderStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không hợp lệ."));

        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalAmount(subtotal)
                .totalPrice(grandTotal)
                .orderStatus(orderStatus)
                .shippingAddress(fullShippingAddress)
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(subtotal * 0.08)
                .transactionId(null)
                .payments(new ArrayList<>())
                .build();


        Order savedOrder = orderRepository.save(order);
        log.info("✅ Đơn hàng PayPal đã được tạo với ID: {}", savedOrder.getId());

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
            // Gọi service tạo đơn hàng PayPal
            String returnUrl = "http://localhost:4200/client/usd/en/paypal-success"; // đổi nếu cần
            String cancelUrl = "http://localhost:4200/client/usd/en/paypal-cancel";

            String paypalApprovalUrl = paypalService.createOrder(grandTotal, returnUrl, cancelUrl);
            log.info("💳 URL thanh toán PayPal: {}", paypalApprovalUrl);

            // Trừ tồn kho luôn nếu bạn muốn (hoặc chờ capture xong mới trừ)
            subtractInventoryForOrder(savedOrder);

            return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paypalApprovalUrl));

        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo URL thanh toán PayPal: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi tạo URL thanh toán PayPal.");
        }
    }


    private void subtractInventoryForOrder(Order order) {
        // Lấy tất cả order details của đơn hàng
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

        for (OrderDetail detail : orderDetails) {
            ProductVariant productVariant = detail.getProductVariant();
            int quantity = detail.getQuantity();

            // Tìm inventory trong warehouse ID 1
            Inventory warehouseInventory = inventoryRepository
                    .findByWarehouseIdAndProductVariantId(1L, productVariant.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Không tìm thấy inventory cho sản phẩm " + productVariant.getId() +
                                    " trong kho ID 1"));

            // Kiểm tra số lượng tồn kho
            if (warehouseInventory.getQuantityInStock() < quantity) {
                throw new IllegalStateException(
                        "Không đủ tồn kho cho sản phẩm " + productVariant.getProduct().getId() +
                                " (ID: " + productVariant.getId() + ")");
            }

            // Trừ inventory
            warehouseInventory.setQuantityInStock(warehouseInventory.getQuantityInStock() - quantity);
            inventoryRepository.save(warehouseInventory);

            log.info("✅ Đã trừ {} sản phẩm {} từ kho",
                    quantity, productVariant.getProduct().getId());
        }
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus cancelledStatus = orderStatusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new ResourceNotFoundException("OrderStatus CANCELLED not found"));

        order.setOrderStatus(cancelledStatus);
        orderRepository.save(order);
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


    public Page<GetAllOrderAdmin> getFilteredOrders(
            Long orderId,
            String status,
            String shippingAddress,
            Double minPrice,
            Double maxPrice,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            LocalDateTime updateFromDate,
            LocalDateTime updateToDate,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Specification<Order> spec = OrderSpecification
                .filterOrders(orderId, status, shippingAddress, minPrice,
                        maxPrice, fromDate, toDate, updateFromDate, updateToDate);

//        Specification<Order> spec = OrderSpecification.filterOrders(orderId, status, shippingAddress, minPrice, maxPrice, fromDate, toDate, updateFromDate, updateToDate)
//                .and((root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get("user")));

        // Tạo `Sort` theo sortBy và sortDirection
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return orderRepository.findAll(spec, pageable).map(GetAllOrderAdmin::fromGetAllOrderAdmin);
    }


    @Transactional
    public GetAllOrderAdmin updateOrderStatus(Long orderId, String status) {
        log.info("Updating order {} to status: {}", orderId, status);

        // 1️⃣ Kiểm tra đơn hàng có tồn tại không
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));


        // 2️⃣ Lấy trạng thái thanh toán của đơn hàng
        Payment orderPayment = paymentRepository.findTopByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment information not found for order: " + orderId));

        String paymentStatus = orderPayment.getStatus(); // Lấy trạng thái thanh toán

        // 3️⃣ Nếu paymentStatus là UNPAID và muốn cập nhật thành DONE -> Chặn cập nhật
        if ("UNPAID".equals(paymentStatus) && "DONE".equals(status)) {
            throw new BadRequestException("Cannot update order to DONE when payment is UNPAID.");
        }

        Optional<OrderStatus> orderStatusOptional = orderStatusRepository.findFirstByStatusName(status);

        if (!orderStatusOptional.isPresent()) {
            throw new BadRequestException("Invalid order status: " + status);
        }

        // Lấy trạng thái
        OrderStatus updatedStatus = orderStatusOptional.get();

        // 5️⃣ Kiểm tra trạng thái mới có hợp lệ không
        if (!isValidStatusTransition(order.getOrderStatus().getStatusName(), status)) {
            throw new BadRequestException("Cannot update order status from " +
                    order.getOrderStatus().getStatusName() + " to " + status);
        }

        // 6️⃣ Cập nhật trạng thái
        if (!order.getOrderStatus().getStatusName().equals(updatedStatus.getStatusName())) {
            order.setOrderStatus(updatedStatus);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        return GetAllOrderAdmin.fromGetAllOrderAdmin(order);
    }


    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        List<String> statusFlow = List.of("PENDING", "PROCESSING", "SHIPPED", "DELIVERED","CANCELLED", "DONE");

        int currentIndex = statusFlow.indexOf(currentStatus);
        int newIndex = statusFlow.indexOf(newStatus);

        // Cho phép cập nhật trực tiếp từ PENDING → DONE
//        if ("PENDING".equals(currentStatus) && "DONE".equals(newStatus)) {
//            return true;
//        }

        // Cho phép cập nhật trực tiếp từ PROCESSING → DONE
        if ("PROCESSING".equals(currentStatus) && "DONE".equals(newStatus)) {
            return true;
        }

        // Cho phép cập nhật trực tiếp từ PENDING → CANCELLED
        if ("PENDING".equals(currentStatus) && "CANCELLED".equals(newStatus)) {
            return true;
        }

        return currentIndex < newIndex;
    }

    @Transactional
    public GetAllOrderAdmin updatePaymentStatus(Long orderId, String paymentStatus) {
        // 1️⃣ Kiểm tra đơn hàng có tồn tại không
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // 2️⃣ Lấy phương thức thanh toán từ danh sách Payment
        String paymentMethod = order.getPayments().stream()
                .findFirst()
                .map(payment -> payment.getPaymentMethod().getMethodName()) // Lấy tên phương thức thanh toán
                .orElseThrow(() -> new BadRequestException("Payment method not found for order ID: " + orderId));

        // 3️⃣ Kiểm tra phương thức thanh toán có phải là COD không
        if (!"COD".equalsIgnoreCase(paymentMethod)) {
            throw new BadRequestException("Only COD orders can update payment status manually.");
        }

        // 4️⃣ Kiểm tra trạng thái thanh toán có hợp lệ không
        List<String> validPaymentStatuses = List.of("PAID", "UNPAID");
        if (!validPaymentStatuses.contains(paymentStatus.toUpperCase())) {
            throw new BadRequestException("Invalid payment status: " + paymentStatus);
        }

        // 5️⃣ Cập nhật trạng thái thanh toán
        order.getPayments().forEach(payment -> payment.setStatus(paymentStatus.toUpperCase()));
        orderRepository.save(order);

        return GetAllOrderAdmin.fromGetAllOrderAdmin(order);
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
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new DataNotFoundException("Staff not found with ID: " + staffId));

        User user = (request.getUserId() != null) ?
                userRepository.findById(request.getUserId()).orElse(null) : null;

        Cart cart = cartRepository.findByUser_Id(staffId)
                .orElseThrow(() -> new DataNotFoundException("Cart not found for Staff ID: " + staffId));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new DataNotFoundException("Store not found with ID: " + request.getStoreId()));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty, cannot create order.");
        }

        Coupon coupon = (request.getCouponId() != null) ?
                couponRepository.findById(request.getCouponId()).orElse(null) : null;

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new DataNotFoundException("Payment method not found"));

        Order order = Order.builder()
                .user(user)
                .store(store)
                .coupon(coupon)
                .totalPrice(request.getTotalPrice())
                .totalAmount(request.getTotalAmount())
                .shippingFee(0D)
                .taxAmount(request.getTaxAmount())
                .shippingAddress(store.getAddress().getFullAddress())
                .orderStatus(orderStatusRepository.findByStatusName("DONE")
                        .orElseThrow(() -> new DataNotFoundException("Order status DONE not found")))
                .build();

        order = orderRepository.save(order);

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

        for (OrderDetail orderDetail : orderDetails) {
            Inventory inventory = inventoryRepository
                    .findByStoreIdAndProductVariantId(store.getId(), orderDetail.getProductVariant().getId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Inventory not found for Product Variant ID: " + orderDetail.getProductVariant().getId()
                                    + " in Store ID: " + store.getId()));
            if (inventory.getQuantityInStock() < orderDetail.getQuantity()) {
                throw new IllegalStateException("Not enough stock for Product Variant ID: "
                        + orderDetail.getProductVariant().getId());
            }
            inventory.setQuantityInStock(inventory.getQuantityInStock() - orderDetail.getQuantity());
            inventoryRepository.save(inventory);
        }

        if (user != null && coupon != null) {
            couponUserRestrictionRepository.deleteByCouponIdAndUserId(user.getId(), coupon.getId());
        }

        // Nếu thanh toán là Cash thì tạo luôn Payment
        if (paymentMethod.getMethodName().equalsIgnoreCase("Cash")) {
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(paymentMethod)
                    .paymentDate(new Date())
                    .amount(order.getTotalPrice())
                    .status("COMPLETED")
                    .transactionCode(request.getTransactionCode() != null ?
                            request.getTransactionCode() : UUID.randomUUID().toString())
                    .build();
            paymentRepository.save(payment);

            return StorePaymentResponse.fromOrder(order);
        }


        if (paymentMethod.getMethodName().equalsIgnoreCase("MOMO")) {
            long storeId = order.getStore().getId();
            String baseOrderId = String.valueOf(order.getId());
            long amount = Math.round(order.getTotalPrice());
            String orderInfo = "Thanh toán đơn hàng #" + baseOrderId;

            Map<String, Object> momoResponse = momoStoreService.createPaymentAtStore(storeId,amount, orderInfo, baseOrderId);
            String payUrl = momoResponse.get("payUrl") != null ? momoResponse.get("payUrl").toString() : null;

            return StorePaymentResponse.builder()
                    .orderId(order.getId())
                    .tax_amount(order.getTotalAmount())
                    .totalPrice(order.getTotalPrice())
                    .payUrl(payUrl)
                    .build();
        }

        throw new IllegalStateException("Unsupported payment method: " + paymentMethod.getMethodName());
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

        double shippingFee = 0.0;  // 7️⃣ Phí vận chuyển = 0 vì khách nhận hàng tại cửa hàng
        double subtotal = totalPrice - discount ; // tổng tiền trước thuế sau giảm giá
        double tax = subtotal * 0.08;  // Thuế 8% trên subtotal
        double grandTotal = Math.round((subtotal + tax + shippingFee) * 100) / 100.0;
        log.info("💰 Tổng tiền đơn hàng sau khi áp dụng mã giảm giá: {}", subtotal);

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
                .totalAmount(subtotal) // tổng trước thuế
                .totalPrice(grandTotal) // tổng sau thuế
                .orderStatus(orderStatus)
                .shippingAddress(store.getAddress().getFullAddress())
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(tax)
                .transactionId(null)
                .store(store)
                .payments(new ArrayList<>())
                .build();

        order.setTotalPrice(grandTotal + shippingFee);
        Order savedOrder = orderRepository.save(order);
        log.info("✅ Đơn hàng đã được tạo với ID: {}", savedOrder.getId());




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
        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(paymentMethod)
                .amount(grandTotal)
                .paymentDate(new Date())
                .status("UNPAID")
                .transactionCode("")
                .build();

        paymentRepository.save(payment);

        savedOrder.getPayments().add(payment);
        orderRepository.save(savedOrder);

        emailService.sendOrderConfirmationEmail(
                savedOrder.getUser().getEmail(),
                StoreOrderResponse.fromOrder(savedOrder,"vi")
        );

        cartService.clearCart(savedOrder.getUser().getId(),"");


        return ResponseEntity.ok(CreateOrderResponse.fromOrder(savedOrder));
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }





    public List<Top3Store> getTop3StoresByRevenue(LocalDate startDate, LocalDate endDate) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        Pageable topThree = PageRequest.of(0, 3);
        return orderRepository.findTop3StoresByRevenue(startDate, endDate, topThree);
    }


}


