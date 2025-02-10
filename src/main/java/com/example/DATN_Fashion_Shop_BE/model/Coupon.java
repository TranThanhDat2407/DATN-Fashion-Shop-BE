package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    private String discountValue;

    @Column(name = "min_order_value", nullable = false)
    private String minOrderValue;

    @Column(name = "user_limit", nullable = false)
    private String userLimit;

    @Column(name = "expiration_date", nullable = false)
    private String expirationDate;

    @Column(name= "is_active")
    private Boolean isActive = true;

    @Column(name = "codes", nullable = false)
    private String code;
}
