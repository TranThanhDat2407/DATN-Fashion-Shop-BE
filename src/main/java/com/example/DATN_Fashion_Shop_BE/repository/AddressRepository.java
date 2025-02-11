package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findAddressById(long id);
}
