package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.CityRevenueResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.TopStoresRevenueResponse;
import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface StoreRepository extends JpaRepository<Store, Long> {
    Page<Store> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Store> findByAddress_CityContainingIgnoreCase(String city, Pageable pageable);
    Page<Store> findByNameContainingIgnoreCaseAndAddress_CityContainingIgnoreCase(String name, String city, Pageable pageable);
    Boolean existsByAddress(Address address);

    @Query(value = "SELECT s.id as storeId, s.name as storeName, a.city, " +
            "COALESCE(SUM(CASE WHEN os.status_name = 'DONE' " +
            "AND (:year = 0 OR YEAR(o.updated_at) = :year) THEN o.total_price ELSE 0 END), 0) as totalRevenue, " +
            ":year as year " +
            "FROM store s " +
            "LEFT JOIN orders o ON s.id = o.store_id " +
            "LEFT JOIN order_status os ON o.status_id = os.id " +
            "LEFT JOIN addresses a ON s.address_id = a.id " +
            "GROUP BY s.id, s.name, a.city " +
            "ORDER BY totalRevenue DESC",
            nativeQuery = true)
    List<TopStoresRevenueResponse> findAllStoresWithRevenueInYear(@Param("year") int year);

    @Query("""
    SELECT new com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.CityRevenueResponse(
        a.city,
        COALESCE(SUM(CASE
            WHEN o.id IS NOT NULL
            AND os.statusName = 'DONE'
            AND (:year = 0 OR YEAR(o.updatedAt) = :year)
            THEN o.totalPrice
            ELSE 0
        END), 0),
        COUNT(DISTINCT s.id),
        :year)
    FROM Store s
    LEFT JOIN s.orders o
    LEFT JOIN o.orderStatus os
    LEFT JOIN s.address a
    GROUP BY a.city
    ORDER BY SUM(CASE
        WHEN o.id IS NOT NULL
        AND os.statusName = 'DONE'
        AND (:year = 0 OR YEAR(o.updatedAt) = :year)
        THEN o.totalPrice
        ELSE 0
    END) DESC""")
    List<CityRevenueResponse> getRevenueByCity(@Param("year") int year);

}
