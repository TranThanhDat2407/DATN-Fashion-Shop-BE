package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository< OrderStatus, Long> {
    Optional<OrderStatus> findByStatusName(String statusName);
    Optional<OrderStatus> findFirstByStatusName(String statusName);
}
