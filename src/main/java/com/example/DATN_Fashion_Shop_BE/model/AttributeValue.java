package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "attributes_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_value_pattern_id")
    private AttributeValuePattern attributeValuePattern;

    @OneToMany(mappedBy = "colorValue")  // Mối quan hệ với màu sắc
    private Set<ProductVariant> productVariantsForColor = new HashSet<>();

    @OneToMany(mappedBy = "sizeValue")  // Mối quan hệ với kích thước
    private Set<ProductVariant> productVariantsForSize = new HashSet<>();

}
