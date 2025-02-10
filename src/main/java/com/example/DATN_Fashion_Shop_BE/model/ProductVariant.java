package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_price", nullable = false)
    private Double salePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_value_id")
    private AttributeValue colorValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_value_id")
    private AttributeValue sizeValue;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToMany(mappedBy = "productVariants")
    private Set<ProductMedia> productMedias = new HashSet<>();

    public Double getAdjustedPrice() {
        if (product.getPromotion() != null &&
                product.getPromotion().getIsActive() &&
                LocalDateTime.now().isAfter(product.getPromotion().getStartDate()) &&
                LocalDateTime.now().isBefore(product.getPromotion().getEndDate())) {
            Double discountPercentage = product.getPromotion().getDiscountPercentage();
            return salePrice - (salePrice * discountPercentage / 100);
        }
        return salePrice;
    }
}
