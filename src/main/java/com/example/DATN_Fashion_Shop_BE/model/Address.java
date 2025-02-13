package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street",columnDefinition = "NVARCHAR(255)", length = 255)
    private String street;

    @Column(name = "district",columnDefinition = "NVARCHAR(255)", length = 100)
    private String district;

    @Column(name = "ward",columnDefinition = "NVARCHAR(255)", length = 100)
    private String ward;

    @Column(name = "city",columnDefinition = "NVARCHAR(255)", length = 100)
    private String city;

    @Column(name = "latitude")
    private Integer latitude;

    @Column(name = "longitude")
    private Integer longitude;


}
