package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", length = 255, nullable = false)
    private String status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToMany
    @JoinTable(
            name = "products_categories", // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "product_id"), // Khóa ngoại từ Product
            inverseJoinColumns = @JoinColumn(name = "category_id") // Khóa ngoại từ Category
    )
    private Set<Category> categories = new HashSet<>();
}
