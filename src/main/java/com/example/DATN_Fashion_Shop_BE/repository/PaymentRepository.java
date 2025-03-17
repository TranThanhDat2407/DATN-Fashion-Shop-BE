package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.order.id = :orderId")
    void updatePaymentStatus(@Param("orderId") Long orderId, @Param("status") String status);


    Optional<Payment> findByOrderId(Long orderId);

}
