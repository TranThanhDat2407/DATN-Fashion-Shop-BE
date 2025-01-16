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

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "products_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductsTranslation> translations;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = true)
    private Promotion promotion;

    public ProductsTranslation getTranslationByLanguage(String langCode) {
        ProductsTranslation translation = translations.stream()
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
