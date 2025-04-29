package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUserId(Long userId);

    @Query("""
        SELECT s FROM Staff s
        WHERE (:storeId IS NULL OR s.store.id = :storeId)
        AND (:id IS NULL OR s.id = :id)
        AND (:name IS NULL OR LOWER(s.user.firstName) LIKE LOWER(CONCAT('%', :name, '%')) 
                           OR LOWER(s.user.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:startDate IS NULL OR :endDate IS NULL OR s.createdAt BETWEEN :startDate AND :endDate)
        AND (:roleId IS NULL OR s.user.role.id = :roleId)
    """)
    Page<Staff> findByFilters(Long storeId, Long id, String name,
                              LocalDateTime startDate, LocalDateTime endDate,
                              Long roleId, Pageable pageable);

    boolean existsByUser_IdAndStore_Id(Long userId, Long storeId);
}
