package com.example.DATN_Fashion_Shop_BE.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Column(name = "street",columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String street;

    @Column(name = "district",columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String district;

    @Column(name = "ward",columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String ward;

    @Column(name = "city",columnDefinition = "NVARCHAR(MAX)", nullable = false)
    @JsonProperty("province")
    private String city;

    @Column(name = "full_address",columnDefinition = "NVARCHAR(MAX)")
    private String fullAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;


}
