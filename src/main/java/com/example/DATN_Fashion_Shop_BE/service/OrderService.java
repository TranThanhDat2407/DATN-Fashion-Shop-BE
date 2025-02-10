package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.config.GHNConfig;
import com.example.DATN_Fashion_Shop_BE.dto.request.CarItem.CarItemDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.Ghn.PreviewOrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod.ShippingMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.GhnPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.PreviewOrderResponse;

import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingOrderReviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;

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
    private final ShippingService shippingService;
    private final RestTemplate restTemplate;
    private final GHNConfig ghnConfig;
    private final UserAddressRepository userAddressRepository;
    private final ShippingMethodRepository shippingMethodRepository;


    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
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

        // 📦 4️⃣ Chuẩn bị danh sách sản phẩm gửi GHN
        List<CarItemDTO> items = cartItems.stream()
                .map(item -> CarItemDTO.builder()
                        .name(item.getProductVariant().getProduct()
                                .getTranslations().stream()
                                .filter(t -> t.getLanguage().equals("vi")) // Chọn ngôn ngữ tiếng Việt
                                .findFirst()
                                .map(ProductsTranslation::getName) // Lấy tên sản phẩm
                                .orElse("Sản phẩm chưa có tên")) // Giá trị mặc định nếu không tìm thấy
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // 📍 5️⃣ Lấy địa chỉ giao hàng của user
        UserAddress userAddress = userAddressRepository.findByUser_IdAndIsDefaultTrue(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ mặc định của người dùng."));

        Address address = userAddress.getAddress(); // Lấy Address từ UserAddress
        String fullShippingAddress = String.format("%s, %s, %s, %s",
                address.getStreet(), address.getWard(), address.getDistrict(), address.getCity());

        ShippingMethodRequest shippingRequest = ShippingMethodRequest.builder()
                .to_name(userAddress.getFirstName() + " " + userAddress.getLastName())
                .to_phone(userAddress.getPhone())
                .to_address(address.getStreet())
                .to_ward_name(address.getWard())
                .to_district_name(address.getDistrict())
                .to_province_name(address.getCity())
                .weight(500)
                .items(items)
                .build();

        double shippingFee;
        try {
            ShippingOrderReviewResponse shippingResponse = shippingService.getShippingFee(shippingRequest);
            shippingFee = shippingResponse.getFee().getMain_service();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy phí vận chuyển từ GHN: " + e.getMessage());
        }

        // 🛍 6️⃣ Tính tổng tiền đơn hàng
        double finalAmount = totalAmount - discount + shippingFee;

        ShippingMethod shippingMethod = shippingMethodRepository.findById(orderRequest.getShippingMethodId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức vận chuyển hợp lệ."));

        // 📦 7️⃣ Tạo Order
        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalPrice(totalAmount)
                .totalAmount(finalAmount)
                .orderStatus(OrderStatus.builder().id(1L).build())
                .shippingAddress(fullShippingAddress) // Lưu địa chỉ dạng String
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .taxAmount(0.0)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 🛒 8️⃣ Lưu OrderDetail
        List<OrderDetail> orderDetails = cartItems.stream().map(item ->
                OrderDetail.builder()
                        .order(savedOrder)
                        .product(item.getProductVariant().getProduct())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getProductVariant().getSalePrice())
                        .totalPrice(item.getProductVariant().getSalePrice() * item.getQuantity())
                        .build()).collect(Collectors.toList());

        orderDetailRepository.saveAll(orderDetails);

        // 💳 9️⃣ Xử lý thanh toán
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

        // 🛒 🔟 Xóa giỏ hàng sau khi đặt hàng thành công
        cartItemRepository.deleteAll(cartItems);
        cartRepository.delete(cart);

        return savedOrder;
    }



    public PreviewOrderResponse previewOrder(PreviewOrderRequest request) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/preview";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ShopId", String.valueOf(ghnConfig.getShopId()));
        headers.set("Token", ghnConfig.getToken());
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<PreviewOrderRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<GhnPreviewResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, GhnPreviewResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return PreviewOrderResponse.fromGHNResponse(response.getBody());
            } else {
                throw new RuntimeException("Lấy thông tin preview đơn hàng thất bại! Mã lỗi: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Lỗi từ GHN: " + e.getMessage());
        }
    }

//    public GhnCreateOrderResponse createOrder(GhnCreateOrderRequest request) {
//        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Token", ghnConfig.getToken());
//        headers.set("ShopId", String.valueOf(ghnConfig.getShopId()));
//        headers.set("User-Agent", "Mozilla/5.0");
//
//        HttpEntity<GhnCreateOrderRequest> entity = new HttpEntity<>(request, headers);
//
//        try {
//            ResponseEntity<GhnCreateOrderResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, GhnCreateOrderResponse.class);
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return response.getBody();
//            } else {
//                throw new RuntimeException("Tạo đơn hàng thất bại! Mã lỗi: " + response.getStatusCode());
//            }
//        } catch (HttpClientErrorException e) {
//            throw new RuntimeException("Lỗi từ GHN: " + e.getResponseBodyAsString());
//        }
//    }




}
