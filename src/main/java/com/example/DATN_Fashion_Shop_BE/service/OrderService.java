package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final PaymentMethodRepository paymentMethodRepository;


    @Transactional
    public Order placeOrder(OrderRequest orderRequest) {
        // Lấy giỏ hàng của user
        Cart cart = cartRepository.findByUser_Id(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng");
        }

        // Tính tổng tiền sản phẩm
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> BigDecimal.valueOf(item.getProductVariant().getSalePrice())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Áp dụng mã giảm giá nếu có
        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (orderRequest.getCouponId() != null) {
            coupon = couponRepository.findById(orderRequest.getCouponId()).orElse(null);
            if (coupon != null && coupon.getIsActive()) {
                discount = new BigDecimal(coupon.getDiscountValue());
            }
        }

        // Tính phí vận chuyển
        ShippingMethod shippingMethod = shippingMethodRepository.findById(orderRequest.getShippingMethodId())
                .orElseThrow(() -> new RuntimeException("Phương thức vận chuyển không hợp lệ"));

        BigDecimal shippingFee = new BigDecimal(shippingMethod.getDescription()); // Giả sử mô tả chứa giá ship

        // Tổng tiền đơn hàng sau giảm giá + phí vận chuyển
        BigDecimal finalAmount = totalAmount.subtract(discount).add(shippingFee);

        // Tạo đơn hàng
        Order order = Order.builder()
                .user(User.builder().id(orderRequest.getUserId()).build())
                .coupon(coupon)
                .totalPrice(totalAmount)
                .totalAmount(finalAmount)
                .orderStatus(OrderStatus.builder().id(1L).build()) // 1: Đang xử lý
                .shippingAddress(orderRequest.getShippingAddress())
                .shippingMethod(shippingMethod)
                .shippingFee(shippingFee.toString())
                .build();
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
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ"));

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentDate(new Date())
                .amount(finalAmount.doubleValue())
                .status("PENDING") // Chờ thanh toán
                .transactionCode(UUID.randomUUID().toString())
                .build();
        paymentRepository.save(payment);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        cartItemRepository.deleteAll(cartItems);
        cartRepository.delete(cart);

        return order;
    }
}
