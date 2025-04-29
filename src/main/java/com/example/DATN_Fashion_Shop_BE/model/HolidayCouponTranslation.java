package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "holiday_coupon_translation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayCouponTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_type", nullable = false)
    private String couponType;

    @Column(name = "name", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String name; // Tiêu đề bản dịch

    @Column(name = "description", columnDefinition = "NVARCHAR(300)", nullable = false)
    private String description; // Mô tả bản dịch

    @Column(name = "language_code", length = 10, nullable = false)
    private String languageCode; // Mã ngôn ngữ (vi, en, jp)
}
