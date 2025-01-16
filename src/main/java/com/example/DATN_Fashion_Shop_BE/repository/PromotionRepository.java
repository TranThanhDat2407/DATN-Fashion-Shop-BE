package com.example.DATN_Fashion_Shop_BE.repository;


import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByIsActiveTrue();

    @Query("SELECT p FROM Promotion p WHERE p.startDate >= :startDate AND p.endDate <= :endDate")
    Page<Promotion> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);
}
