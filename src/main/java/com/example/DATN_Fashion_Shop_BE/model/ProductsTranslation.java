package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductsTranslation extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "material", columnDefinition = "NVARCHAR(MAX)")
    private String material;

    @Column(name = "care", columnDefinition = "NVARCHAR(MAX)")
    private String care;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;
}
