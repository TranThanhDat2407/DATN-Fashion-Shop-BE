package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "open_hour")
    private LocalDateTime openHour;

    @Column(name = "close_hour")
    private LocalDateTime closeHour;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<Order> orders;

}
