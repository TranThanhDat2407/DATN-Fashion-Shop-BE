package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "attribute_value_pattern")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValuePattern extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Khóa chính

    @Column(name = "pattern_name", nullable = false)
    private String name; // Tên của kiểu khuôn (ví dụ: "Basic Size", "Shoe Size", ...)

    @Column(name = "pattern_type", nullable = false)
    private String type; // Loại pattern, ví dụ: "clothing", "shoes", v.v.

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // Trạng thái hoạt động của pattern

    @OneToMany(mappedBy = "attributeValuePattern", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AttributeValue> attributeValues = new HashSet<>();
}
