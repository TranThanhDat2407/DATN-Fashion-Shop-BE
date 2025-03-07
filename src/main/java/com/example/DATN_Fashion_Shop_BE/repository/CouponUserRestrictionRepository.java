package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.CouponUserRestriction;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponUserRestrictionRepository extends JpaRepository<CouponUserRestriction, Long> {
    @Query("SELECT c.user.id FROM CouponUserRestriction c WHERE c.coupon.id = :couponId")
    List<Long> findUserIdsByCouponId(@Param("couponId") Long couponId);
    Optional<CouponUserRestriction> findByUserIdAndCouponId(Long userId, Long couponId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CouponUserRestriction c WHERE c.coupon.id = :couponId AND c.user.id = :userId")
    void deleteByCouponIdAndUserId(@Param("couponId") Long couponId, @Param("userId") Long userId);

}
