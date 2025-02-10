package com.example.DATN_Fashion_Shop_BE.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Category extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Audited
    @Column(name= "is_active")
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    @ManyToMany(mappedBy = "categories")
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CategoriesTranslation> translations;

    @Version
    private Long version;

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
