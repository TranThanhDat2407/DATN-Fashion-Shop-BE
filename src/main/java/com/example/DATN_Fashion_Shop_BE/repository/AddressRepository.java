package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.UserAddress;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT a FROM Address a JOIN UserAddress ua ON a.id = ua.address.id WHERE ua.user.id = :userId")
    List<Address> findAllByUserId(@Param("userId") Long userId);


}
