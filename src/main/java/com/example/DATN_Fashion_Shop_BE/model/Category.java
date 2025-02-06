package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name= "is_active")
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    @ManyToMany(mappedBy = "categories")
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoriesTranslation> translations;

    public String getTranslationByLanguage(String languageCode) {
        return translations.stream()
                .filter(t -> t.getLanguage().getCode().equals(languageCode))
                .map(CategoriesTranslation::getName)
                .findFirst()
                .orElse(null); // Hoặc giá trị mặc định
    }

    public Set<Category> getAllSubCategories() {
        Set<Category> subCategories = new HashSet<>();
        for (Category child : this.getSubCategories()) {
            subCategories.add(child);
            subCategories.addAll(child.getAllSubCategories()); // Recursively adding subcategories
        }
        return subCategories;
    }

    @OneToMany(mappedBy = "parentCategory")
    private Set<Category> subCategories = new HashSet<>();
}
