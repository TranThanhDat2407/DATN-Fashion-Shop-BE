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
import org.springframework.web.client.RestTemplate;


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


    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        // Lấy giỏ hàng của user
        Cart cart = cartRepository.findByUser_Id(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException(localizationUtils
                        .getLocalizedMessage(MessageKeys.CART_NOT_FOUND, orderRequest.getUserId())));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException(localizationUtils
                    .getLocalizedMessage(MessageKeys.CART_ITEM_NOT_FOUND, cart.getId()));
        }

        // Tính tổng tiền sản phẩm
        double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getProductVariant().getSalePrice() * item.getQuantity())
                .sum();

        // Áp dụng mã giảm giá nếu có
        double discount = 0.0;
        Coupon coupon = null;
        if (orderRequest.getCouponId() != null) {
            coupon = couponRepository.findById(orderRequest.getCouponId()).orElse(null);
            if (coupon != null && coupon.getIsActive()) {
                discount = coupon.getDiscountValue();
            }
        }

        // Chuẩn bị danh sách sản phẩm gửi GHN
        List<CarItemDTO> items = cartItems.stream()
                .map(item -> CarItemDTO.builder()
                        .name(item.getCart().getSessionId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // Chuẩn bị request cho GHN
        ShippingMethodRequest shippingRequest = ShippingMethodRequest.builder()
                .to_name("")
                .to_phone("")
                .to_address("")
                .to_ward_name("")
                .to_district_name("")
                .to_province_name("")
                .weight(500)
                .items(items)
                .build();

        // Gọi API GHN để lấy phí vận chuyển
        ShippingOrderReviewResponse shippingResponse = shippingService.getShippingFee(shippingRequest);

        // Kiểm tra phản hồi từ GHN
//        if (shippingResponse == null || shippingResponse.getFee() == null || shippingResponse.getFee().getMain_service() == null) {
//            throw new RuntimeException("Lỗi: GHN không trả về thông tin phí vận chuyển hợp lệ.");
//        }

        double shippingFee = shippingResponse.getFee().getMain_service();

        // Tổng tiền đơn hàng sau giảm giá + phí vận chuyển
        double finalAmount = totalAmount - discount + shippingFee;

        // Tạo đơn hàng
        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalPrice(totalAmount)
                .totalAmount(finalAmount)
                .orderStatus(OrderStatus.builder().id(1L).build())
                .shippingAddress(orderRequest.getShippingAddress())
                .shippingFee(shippingFee) // Lưu phí vận chuyển vào đơn hàng
                .taxAmount(0.0)
                .build();

        // Lưu đơn hàng vào database
        order = orderRepository.save(order);

        // Tạo danh sách OrderDetail
        for (CartItem item : cartItems) {
            orderDetailRepository.save(OrderDetail.builder()
                    .order(order)
                    .product(item.getProductVariant().getProduct())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getProductVariant().getSalePrice())
                    .totalPrice(item.getProductVariant().getSalePrice() * item.getQuantity())
                    .build());
        }

        // Xử lý thanh toán
        PaymentMethod paymentMethod = paymentMethodRepository.findById(orderRequest.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException(localizationUtils
                        .getLocalizedMessage(MessageKeys.PAYMENT_METHOD_NOT_VALID)));

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentDate(new Date())
                .amount(finalAmount)
                .status("PENDING")
                .transactionCode(UUID.randomUUID().toString())
                .build();
        paymentRepository.save(payment);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        cartItemRepository.deleteAll(cartItems);
        cartRepository.delete(cart);

        return order;
    }


    public PreviewOrderResponse previewOrder(PreviewOrderRequest request) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/preview";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ShopId", ghnConfig.getShopId());
        headers.set("Token", ghnConfig.getToken());

        HttpEntity<PreviewOrderRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<GhnPreviewResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, GhnPreviewResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return PreviewOrderResponse.fromGHNResponse(response.getBody());
        } else {
            throw new RuntimeException("Lấy thông tin preview đơn hàng thất bại!");
        }
    }





}
