package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.UserCouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponUsageRepository extends JpaRepository<UserCouponUsage, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long id);

    List<Long> findUsedCouponIdsByUserId(Long userId);
}
