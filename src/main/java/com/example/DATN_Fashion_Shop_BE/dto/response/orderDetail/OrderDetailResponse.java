package com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail;

import com.example.DATN_Fashion_Shop_BE.dto.response.payment.PaymentMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.CreateProductResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductTranslationResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductVariantResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.service.EmailService;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {
    private Long orderDetailId;
    private Long orderId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private ProductVariantResponse productVariant;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String paymentMethod;
    private Double tax;
    private Double shippingFee;
    private Double grandTotal;
    private String imageUrl;
    private static final Logger log = LoggerFactory.getLogger(OrderDetailResponse.class);


    public static OrderDetailResponse fromOrderDetail(OrderDetail orderDetail, List<UserAddressResponse> userAddressResponses) {
        Product product = orderDetail.getProductVariant().getProduct();
        Order order = orderDetail.getOrder(); // Lấy Order từ OrderDetail


        ProductVariant variant = orderDetail.getProductVariant();
        AttributeValue color = variant.getColorValue();
        String productImage = null;
        if (product.getMedias() != null && !product.getMedias().isEmpty()) {
            productImage = product.getMedias().stream()
                    .filter(media -> media.getColorValue() != null && color != null && media.getColorValue().getId().equals(color.getId())) // So sánh bằng ID thay vì equals()
                    .map(ProductMedia::getMediaUrl)
                    .findFirst()
                    .orElse(product.getMedias().get(0).getMediaUrl()); // Nếu không có, lấy ảnh đầu tiên
        }


        UserAddressResponse defaultAddress = (userAddressResponses != null && !userAddressResponses.isEmpty())
                ? userAddressResponses.stream()
                .filter(UserAddressResponse::getIsDefault)
                .findFirst()
                .orElse(userAddressResponses.get(0))
                : null;

        log.info("📌 Default Address: {}", defaultAddress);
        String recipientName = (defaultAddress != null) ? defaultAddress.getLastName() + " " + defaultAddress.getFirstName() : null;
        String recipientPhone = (defaultAddress != null) ? defaultAddress.getPhone() : null;

        log.info("📌 Recipient Name: {}", recipientName);
        log.info("📌 Recipient Phone: {}", recipientPhone);

        List<PaymentMethodResponse> paymentMethods = (order.getPayments() != null)
                ? order.getPayments().stream()
                .map(payment -> PaymentMethodResponse.fromPaymentMethod(payment.getPaymentMethod()))
                .collect(Collectors.toList())
                : List.of();


        String paymentMethodNames = (paymentMethods != null && !paymentMethods.isEmpty())
                ? paymentMethods.stream().map(PaymentMethodResponse::getMethodName).collect(Collectors.joining(", "))
                : "Thanh toán khi nhận hàng";

        log.info("📌 Order Payments: {}", order.getPayments());


        // Kiểm tra dữ liệu
        log.info("✅ Hình ảnh: " + productImage);
        log.info("✅ Sản phẩm: " + orderDetail.getProductVariant());
        log.info("✅ Số lượng: " + orderDetail.getQuantity());
        log.info("✅ Màu: " + (variant.getColorValue() != null ? variant.getColorValue().getValueName() : "Không có"));
        log.info("✅ Size: " + (variant.getSizeValue() != null ? variant.getSizeValue().getValueName() : "Không có"));
        log.info("✅ Giá: " + orderDetail.getTotalPrice());


        return OrderDetailResponse.builder()
                .orderDetailId(orderDetail.getId())
                .orderId(order.getId())
                .quantity(orderDetail.getQuantity())
                .unitPrice(orderDetail.getUnitPrice())
                .totalPrice(orderDetail.getTotalPrice())
                .imageUrl(productImage)
                .productVariant(ProductVariantResponse.fromProductVariant(orderDetail.getProductVariant()))
                .recipientName(defaultAddress != null ? defaultAddress.getFirstName() + " " + defaultAddress.getLastName() : null)
                .recipientPhone(defaultAddress != null ? defaultAddress.getPhone() : null)
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(paymentMethodNames)
                .tax(order.getTaxAmount())
                .shippingFee(order.getShippingFee())
                .grandTotal(order.getTotalPrice())
                .build();

    }

}
