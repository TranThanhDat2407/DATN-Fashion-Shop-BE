package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.CouponUserRestriction;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CouponUserRestrictionRepository extends JpaRepository<CouponUserRestriction, Long> {
    @Query("SELECT c.user.id FROM CouponUserRestriction c WHERE c.coupon.id = :couponId")
    List<Long> findUserIdsByCouponId(@Param("couponId") Long couponId);

}
