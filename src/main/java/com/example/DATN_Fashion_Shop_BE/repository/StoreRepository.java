package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface StoreRepository extends JpaRepository<Store, Long> {
    Page<Store> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Store> findByAddress_CityContainingIgnoreCase(String city, Pageable pageable);
    Page<Store> findByNameContainingIgnoreCaseAndAddress_CityContainingIgnoreCase(String name, String city, Pageable pageable);
    Boolean existsByAddress(Address address);
}
