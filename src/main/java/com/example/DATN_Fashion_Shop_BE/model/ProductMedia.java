package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "product_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductMedia extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "model_height")
    private Integer modelHeight; // Chiều cao của mẫu sản phẩm trong hình ảnh

    @ManyToOne
    @JoinColumn(name = "color_value_id", referencedColumnName = "id")
    private AttributeValue colorValue; // Liên kết với bảng Attributes_Values

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Liên kết với bảng Products

    @ManyToMany
    @JoinTable(
            name = "media_variants", // Bảng trung gian
            joinColumns = @JoinColumn(name = "product_media_id"), // FK tới ProductMedia
            inverseJoinColumns = @JoinColumn(name = "product_variant_id") // FK tới ProductVariant
    )
    private Set<ProductVariant> productVariants = new HashSet<>();
}
