package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.UserCouponUsage;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponUsageRepository extends JpaRepository<UserCouponUsage, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long id);

    @Query("SELECT u.coupon.id FROM UserCouponUsage u WHERE u.user.id = :userId")
    List<Long> findUsedCouponIdsByUserId(@Param("userId") Long userId);

    void deleteByCouponId(Long id);
}
