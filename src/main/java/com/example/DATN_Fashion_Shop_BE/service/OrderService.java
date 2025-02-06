package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.CarItem.CarItemDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.shippingMethod.ShippingMethodRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.OrderPreviewResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingOrderReviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

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



    @Transactional
    public Order placeOrder(OrderRequest orderRequest) {
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
        List<CarItemDTO> sampleItems = Arrays.asList(
                CarItemDTO.builder().name("Áo thun").quantity(2).build(),
                CarItemDTO.builder().name("Quần jean").quantity(1).build()
        );
        // Chuẩn bị request cho GHN
        ShippingMethodRequest shippingRequest = ShippingMethodRequest.builder()
                .to_name("Nguyễn Văn A")
                .to_phone("0399787124")
                .to_address("211/132/A9 hẻm Hoàng Hoa Thám, Phường 5, Phú Nhuận, HCM, Việt Nam")
                .to_ward_name("Phường 5")
                .to_district_name("Quận Phú Nhuận")
                .to_province_name("HCM")
                .weight(500)
                .items(sampleItems)
                .build();


        // Gọi API GHN để lấy phí vận chuyển
        ShippingOrderReviewResponse shippingResponse = ShippingService.getShippingFee(shippingRequest);
        System.out.println("Shipping response: " + shippingResponse);  // Log phản hồi để kiểm tra

// Kiểm tra xem fee có phải là null không
        if (shippingResponse == null || shippingResponse.getFee() == null) {
            throw new RuntimeException("Lỗi: GHN không trả về thông tin phí vận chuyển.");
        }

// Kiểm tra phí chính (mainService)
        if (shippingResponse.getFee().getMain_service() == null) {
            throw new RuntimeException("Lỗi: GHN không trả về phí chính.");
        }

        double shippingFee = shippingResponse.getFee().getMain_service();

// Kiểm tra xem phí vận chuyển có hợp lệ không
        if (shippingFee == 0) {
            throw new RuntimeException("Lỗi: GHN không trả về phí vận chuyển.");
        }

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




    @Transactional
    public OrderPreviewResponse getOrderPreview(OrderRequest orderRequest) {
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

        // Tính thuế giá trị gia tăng (VAT)
        double taxAmount = totalAmount * 0.1; // Giả sử thuế là 10%

        // Chuẩn bị request cho GHN để lấy phí vận chuyển
        ShippingMethodRequest shippingRequest = ShippingMethodRequest.builder()
                .to_name("Nguyễn Văn A")
                .to_phone("0399787124")
                .to_address(orderRequest.getShippingAddress())
                .to_ward_name("Phường 5")
                .to_district_name("Quận Phú Nhuận")
                .to_province_name("HCM")
                .weight(500)
                .items(Arrays.asList(
                        CarItemDTO.builder().name("Áo thun").quantity(2).build(),
                        CarItemDTO.builder().name("Quần jean").quantity(1).build()
                ))
                .build();

        // Gọi API GHN để lấy phí vận chuyển
        ShippingOrderReviewResponse shippingResponse = ShippingService.getShippingFee(shippingRequest);
        if (shippingResponse == null || shippingResponse.getFee() == null || shippingResponse.getFee().getMain_service() == null) {
            throw new RuntimeException("Lỗi: GHN không trả về thông tin phí vận chuyển.");
        }

        double shippingFee = shippingResponse.getFee().getMain_service();

        // Tính tổng giá trị đơn hàng (giỏ hàng + thuế + phí vận chuyển)
        double finalAmount = totalAmount - discount + taxAmount + shippingFee;

        // Trả về thông tin tổng giá trị đơn hàng cho người dùng
        OrderPreviewResponse previewResponse = new OrderPreviewResponse();
        previewResponse.setTotalAmount(totalAmount);
        previewResponse.setDiscount(discount);
        previewResponse.setTaxAmount(taxAmount);
        previewResponse.setShippingFee(shippingFee);
        previewResponse.setFinalAmount(finalAmount);

        return previewResponse;
    }


}
