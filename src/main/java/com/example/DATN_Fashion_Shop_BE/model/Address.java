package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street",columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String street;

    @Column(name = "district",columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String district;

    @Column(name = "ward",columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String ward;

    @Column(name = "city",columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String city;

    @Column(name = "latitude")
    private Integer latitude;

    @Column(name = "longitude")
    private Integer longitude;
}
