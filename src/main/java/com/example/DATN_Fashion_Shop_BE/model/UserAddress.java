package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "first_name",columnDefinition = "NVARCHAR(255)", length = 100)
    private String firstName;

    @Column(name = "last_name",columnDefinition = "NVARCHAR(255)", length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;


}
