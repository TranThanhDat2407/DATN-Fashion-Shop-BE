package com.example.DATN_Fashion_Shop_BE.model;

import com.example.DATN_Fashion_Shop_BE.config.CouponConfig;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coupon_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponConfigEntity extends CouponConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type; // VD: "sinhnhat", "women_tet_2025"
    private String discountType; // % hoặc VNĐ
    private Float discountValue;
    private Float minOrderValue;
    private int expirationDays;
    private String imageUrl;
}
