package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Banner extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "media_url", length = 255)
    private String mediaUrl;

    @Column(name = "redirect_url", length = 255)
    private String redirectUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "activation_date")
    private LocalDateTime activationDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "banner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BannersTranslation> translations = new HashSet<>();
}
