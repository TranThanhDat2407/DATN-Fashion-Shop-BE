package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(name = "purchased_size", columnDefinition = "NVARCHAR(255)")
    private String purchasedSize;

    @Column(name = "fit", columnDefinition = "NVARCHAR(255)")
    private String fit;

    @Column(name = "nickname", columnDefinition = "NVARCHAR(255)")
    private String nickname;

    @Column(name = "gender", columnDefinition = "NVARCHAR(255)")
    private String gender;

    @Column(name = "age_group", columnDefinition = "NVARCHAR(255)")
    private String ageGroup;

    @Column(name = "height", columnDefinition = "NVARCHAR(255)")
    private String height;

    @Column(name = "weight", columnDefinition = "NVARCHAR(255)")
    private String weight;

    @Column(name = "shoe_size", columnDefinition = "NVARCHAR(255)")
    private String shoeSize;

    @Column(name = "location", columnDefinition = "NVARCHAR(255)")
    private String location;

    @Column(name = "review_rate", columnDefinition = "NVARCHAR(255)")
    private String reviewRate;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
