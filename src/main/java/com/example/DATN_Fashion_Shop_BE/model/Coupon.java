package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Coupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    private Float discountValue;

    @Column(name = "min_order_value", nullable = false)
    private Float minOrderValue;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name= "is_active")
    private Boolean isActive = true;

    @Column(name = "codes", nullable = false)
    private String code;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "is_global")
    private Boolean isGlobal = false; // Nếu true, mọi user đều có thể sử dụng mã


    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CouponTranslation> translations;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CouponUserRestriction> userRestrictions;

    public CouponTranslation getCouponTranslationByLanguage(String langCode) {
        CouponTranslation translation = translations.stream()
                .filter(t -> t.getLanguage().getCode().equals(langCode)
                        && t.getName() != null && !t.getName().isEmpty())
                .findFirst()
                .orElse(null);

        // Nếu không tìm thấy, trả về bản dịch với mã ngôn ngữ "en"
        if (translation == null) {
            translation = translations.stream()
                    .filter(t -> t.getLanguage().getCode().equals("en"))
                    .findFirst()
                    .orElse(null);
        }

        return translation;
    }

}
