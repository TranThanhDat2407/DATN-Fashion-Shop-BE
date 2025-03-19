package com.example.DATN_Fashion_Shop_BE.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.List;

@Entity
@Table(name = "notification")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "type", nullable = false, length = 255)
    private String type;

    @Column(name = "is_read",nullable = false)
    private Boolean isRead = false;

    @Column(name = "redirect_url", length = 255)
    private String redirectUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<NotificationTranslations> translations;

    public NotificationTranslations getTranslationByLanguage(String langCode) {
        NotificationTranslations translation = translations.stream()
                .filter(t -> t.getLanguage().getCode().equals(langCode)
                        && t.getTitle() != null && !t.getTitle().isEmpty()
                        && t.getMessage() != null && !t.getMessage().isEmpty()
                )
                .findFirst().orElse(null);

        return translation;
    }
}
