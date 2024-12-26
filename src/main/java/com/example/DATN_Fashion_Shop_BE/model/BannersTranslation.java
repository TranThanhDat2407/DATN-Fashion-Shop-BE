package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "banners_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BannersTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "subtitle", nullable = false, length = 255)
    private String subtitle;

    @ManyToOne
    @JoinColumn(name = "banner_id", nullable = false)
    private Banner banner;

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;
}
