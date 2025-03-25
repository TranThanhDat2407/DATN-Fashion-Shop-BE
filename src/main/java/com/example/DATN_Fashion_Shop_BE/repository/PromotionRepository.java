package com.example.DATN_Fashion_Shop_BE.repository;


import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByIsActiveTrue();

    @Query("SELECT p FROM Promotion p WHERE p.startDate >= :startDate AND p.endDate <= :endDate")
    Page<Promotion> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    List<Promotion> findByEndDateBeforeAndIsActiveTrue(LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :now AND p.isActive = false")
    List<Promotion> findByStartDate(@Param("now") LocalDateTime now);

    List<Promotion> findByStartDateBeforeAndEndDateAfter(LocalDateTime startDate, LocalDateTime endDate);
    List<Promotion> findByEndDateBefore(LocalDateTime endDate);

    @Query("SELECT p FROM Promotion p WHERE p.startDate BETWEEN :startDate AND :endDate ORDER BY p.startDate ASC LIMIT 1")
    Optional<Promotion> findPromotionBeforeStartDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    @Query("SELECT p FROM Promotion p")
    Page<Promotion> getAllPromotions(Pageable pageable);
}
