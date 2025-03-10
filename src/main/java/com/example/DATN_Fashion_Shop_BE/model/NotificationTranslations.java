package com.example.DATN_Fashion_Shop_BE.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "notification_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTranslations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String message;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    @Audited(targetAuditMode = org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED)
    private Language language;
}
