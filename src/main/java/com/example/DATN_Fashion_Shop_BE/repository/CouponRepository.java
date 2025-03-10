package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CouponRepository extends JpaRepository<Coupon,Long>,JpaSpecificationExecutor<Coupon> {
    Optional<Coupon> findFirstByCode(String code);;

    List<Coupon> findByIsGlobalTrueAndIsActiveTrue();

    @Query("SELECT c FROM Coupon c JOIN c.userRestrictions cur WHERE cur.user.id = :userId")
    List<Coupon> findCouponsByUserId(@Param("userId") Long userId);

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String couponCode);
}
