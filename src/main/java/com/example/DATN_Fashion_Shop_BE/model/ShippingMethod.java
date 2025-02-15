package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shipping_method")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name= "method_name",  columnDefinition = "NVARCHAR(255)", nullable = false)
    private String methodName;
    @Column(name= "description",  columnDefinition = "NVARCHAR(255)", nullable = false)
    private String description;
}
