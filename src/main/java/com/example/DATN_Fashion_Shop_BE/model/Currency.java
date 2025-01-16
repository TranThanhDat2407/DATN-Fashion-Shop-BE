package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "currencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Currency extends BaseEntity{
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 255, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "symbol", length = 10, columnDefinition = "NVARCHAR(10)")
    private String symbol;

    @Column(name = "rate_to_base")
    private Double rateToBase;

    @Column(name = "is_base")
    private Boolean isBase;
}
