package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "attributes_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeValue extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value_name", length = 255, nullable = false)
    private String valueName;

    @Column(name = "value_img", length = 255)
    private String valueImg;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    // Quan hệ Many-to-Many với AttributeValuePattern
    @ManyToMany(mappedBy = "attributeValues")
    private Set<AttributeValuePattern> attributeValuePatterns = new HashSet<>();

    @OneToMany(mappedBy = "colorValue")  // Ví dụ: mối quan hệ với ProductVariant (nếu dùng cho màu)
    private Set<ProductVariant> productVariantsForColor = new HashSet<>();

    @OneToMany(mappedBy = "sizeValue")  // Ví dụ: mối quan hệ với ProductVariant (nếu dùng cho size)
    private Set<ProductVariant> productVariantsForSize = new HashSet<>();

}
