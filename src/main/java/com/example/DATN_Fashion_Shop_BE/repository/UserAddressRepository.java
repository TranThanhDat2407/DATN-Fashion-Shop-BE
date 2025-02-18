package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository< UserAddress,Long> {

    Optional<UserAddress> findTopByUser_IdAndIsDefaultTrue(Long userId);

    Optional<UserAddress> findByUser_IdAndIsDefaultTrue(Long userId);

    Optional<UserAddress> findByUserIdAndAddressId(Long userId, Long addressId);

    boolean existsByAddressId(Long addressId);

    List<UserAddress> findByUserId(Long userId);
}
