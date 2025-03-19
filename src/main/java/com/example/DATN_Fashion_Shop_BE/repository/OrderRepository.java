package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreMonthlyRevenueResponse;
import com.example.DATN_Fashion_Shop_BE.model.Cart;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(Long userId, Pageable pageable);

    Optional<Order> findById(Long id);

    Page<Order> findByOrderStatus_StatusName(String statusName, Pageable pageable);

    // Lọc theo địa chỉ
    Page<Order> findByShippingAddressContainingIgnoreCase(String shippingAddress, Pageable pageable);

    // Lọc theo khoảng giá
    Page<Order> findByTotalPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // Lọc theo ngày tạo
    Page<Order> findByCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    // Lọc theo ngày cập nhật
    Page<Order> findByUpdatedAtBetween(LocalDateTime updateFromDate, LocalDateTime updateToDate, Pageable pageable);

    @Query(value = "SELECT SUM(o.total_price) FROM orders o WHERE CAST(o.created_at AS DATE) = :date", nativeQuery = true)
    Optional<BigDecimal> findTotalRevenueByDay(@Param("date") LocalDate date);



    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    Optional<BigDecimal> findTotalRevenueByMonth(int year, int month);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE YEAR(o.createdAt) = :year")
    Optional<BigDecimal> findTotalRevenueByYear(int year);



 @Query("SELECT o FROM Order o " +
            "WHERE CAST(o.createdAt AS DATE) = CAST(GETDATE() AS DATE)  AND o.orderStatus.id= 6")
    List<Order> getTotalRevenueToday();

    @Query(value = "SELECT * FROM orders o " +
            "WHERE CAST(o.created_at AS DATE) = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) " +
            "AND o.status_id = 6",
            nativeQuery = true)
    List<Order> getTotalRevenueYesterday();


    @Query("SELECT o  FROM Order o " +
            "WHERE CAST(o.createdAt AS DATE) = CAST(GETDATE() AS DATE)  AND o.orderStatus.id = 6")
    List<Order> getTotalOrderCompleteToday();

    @Query(value = "SELECT * FROM orders o " +
            "WHERE CAST(o.created_at AS DATE) = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) " +
            "AND o.status_id = 6",
            nativeQuery = true)
    List<Order> getTotalOrderYesterday();


    @Query("SELECT o  FROM Order o " +
            "WHERE CAST(o.createdAt AS DATE) = CAST(GETDATE() AS DATE)  AND o.orderStatus.id = 5")
    List<Order> getTotalOrderCancelToday();

    @Query(value = "SELECT * FROM orders o " +
            "WHERE CAST(o.created_at AS DATE) = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) " +
            "AND o.status_id = 5",
            nativeQuery = true)
    List<Order> getTotalOrderCancelYesterday();

    @Query("""
                SELECT o FROM Order o
                LEFT JOIN o.orderStatus os
                LEFT JOIN o.payments p
                LEFT JOIN p.paymentMethod pm
                LEFT JOIN o.shippingMethod sm
                LEFT JOIN o.user u
                WHERE o.store.id = :storeId
                AND (:orderStatusId IS NULL OR os.id = :orderStatusId)
                AND (:paymentMethodId IS NULL OR pm.id = :paymentMethodId)
                AND (:shippingMethodId IS NULL OR sm.id = :shippingMethodId)
                AND (:customerId IS NULL OR u.id = :customerId)
                AND (:staffId IS NULL OR o.createdBy = :staffId)
                AND (:startDate IS NULL OR o.updatedAt >= :startDate)
                AND (:endDate IS NULL OR o.updatedAt <= :endDate)
            """)
    Page<Order> findOrdersByFilters(
            @Param("storeId") Long storeId,
            @Param("orderStatusId") Long orderStatusId,
            @Param("paymentMethodId") Long paymentMethodId,
            @Param("shippingMethodId") Long shippingMethodId,
            @Param("customerId") Long customerId,
            @Param("staffId") Long staffId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT NEW com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreMonthlyRevenueResponse(" +
            "MONTH(o.createdAt), SUM(o.totalPrice)) " +
            "FROM Order o " +
            "WHERE YEAR(o.createdAt) = YEAR(CURRENT_DATE) " +
            "AND o.orderStatus.statusName = 'DONE' " +
            "AND o.store.id = :storeId " +  // Lọc theo storeId
            "GROUP BY MONTH(o.createdAt) " +
            "ORDER BY MONTH(o.createdAt)")
    List<StoreMonthlyRevenueResponse> getMonthlyRevenueByStore(@Param("storeId") Long storeId);
}
