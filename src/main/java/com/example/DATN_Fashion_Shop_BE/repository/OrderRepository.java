package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top3Store;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreDailyRevenueResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreMonthlyRevenueResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreRevenueByDateRangeResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreWeeklyRevenueResponse;
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



    @Query(value = "SELECT SUM(o.total_price) FROM orders o WHERE CAST(o.created_at AS DATE) = :date AND o.status_id = 6", nativeQuery = true)
    Optional<BigDecimal> findTotalRevenueByDay(@Param("date") LocalDate date);



    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    Optional<BigDecimal> findTotalRevenueByMonth(int year, int month);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE YEAR(o.createdAt) = :year")
    Optional<BigDecimal> findTotalRevenueByYear(int year);


    @Query("SELECT o FROM Order o JOIN FETCH o.user u LEFT JOIN FETCH u.userAddresses WHERE o.id = :orderId")
    Optional<Order> findOrderWithUserAndAddresses(@Param("orderId") Long orderId);


    @Query("SELECT NEW com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top3Store(" +
            "s.id, s.name, a.fullAddress, s.phone, SUM(o.totalPrice)) " +
            "FROM Order o " +
            "JOIN o.store s " +
            "JOIN s.address a " +
            "WHERE (:startDate IS NULL OR CAST(o.createdAt AS localdate) >= :startDate) " +
            "AND (:endDate IS NULL OR CAST(o.createdAt AS localdate) <= :endDate) " +
            "GROUP BY s.id, s.name, a.fullAddress, s.phone " +
            "ORDER BY SUM(o.totalPrice) DESC")
    List<Top3Store> findTop3StoresByRevenue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);



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
    List<Order> findOrdersByFilters(
            @Param("storeId") Long storeId,
            @Param("orderStatusId") Long orderStatusId,
            @Param("paymentMethodId") Long paymentMethodId,
            @Param("shippingMethodId") Long shippingMethodId,
            @Param("customerId") Long customerId,
            @Param("staffId") Long staffId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT NEW com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreMonthlyRevenueResponse(" +
            "MONTH(o.updatedAt), SUM(o.totalPrice)) " +
            "FROM Order o " +
            "WHERE YEAR(o.updatedAt) = YEAR(CURRENT_DATE) " +
            "AND o.orderStatus.statusName = 'DONE' " +
            "AND o.store.id = :storeId " +
            "GROUP BY MONTH(o.updatedAt) " +
            "ORDER BY MONTH(o.updatedAt)")
    List<StoreMonthlyRevenueResponse> getMonthlyRevenueByStore(@Param("storeId") Long storeId);

    List<Order> findByStoreIdAndOrderStatus_StatusNameAndUpdatedAtBetween(
            Long storeId, String statusName, LocalDateTime startOfWeek, LocalDateTime endOfWeek);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId " +
            "AND o.user IS NOT NULL AND YEAR(o.createdAt) = :currentYear " +
            "AND o.orderStatus.statusName = 'DONE'")
    long countByStoreIdAndUserIsNotNull(@Param("storeId") Long storeId,
                                        @Param("currentYear") int currentYear);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId " +
            "AND o.user IS NULL AND YEAR(o.createdAt) = :currentYear " +
            "AND o.orderStatus.statusName = 'DONE'")
    long countByStoreIdAndUserIsNull(@Param("storeId") Long storeId,
                                     @Param("currentYear") int currentYear);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.order.store.id = :storeId " +
            "AND p.paymentMethod.id = :paymentMethodId " +
            "AND p.order.orderStatus.statusName = 'DONE'")
    long countByStoreIdAndPaymentMethod(@Param("storeId") Long storeId,
                                        @Param("paymentMethodId") Long paymentMethodId);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
            "WHERE CAST(o.createdAt AS date) = CURRENT_DATE " +
            "AND o.store.id = :storeId " +
            "AND o.orderStatus.statusName = 'DONE'")
    Long getTotalRevenueToday(@Param("storeId") Long storeId);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
            "WHERE YEAR(o.updatedAt) = YEAR(CURRENT_DATE) " +
            "AND MONTH(o.updatedAt) = MONTH(CURRENT_DATE) " +
            "AND o.store.id = :storeId " +
            "AND o.orderStatus.statusName = 'DONE'")
    Long getTotalRevenueThisMonth(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE CAST(o.updatedAt AS date) = CURRENT_DATE " +
            "AND o.store.id = :storeId " +
            "AND o.orderStatus.statusName = 'DONE'")
    Long getTotalOrdersToday(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE YEAR(o.updatedAt) = YEAR(CURRENT_DATE) " +
            "AND MONTH(o.updatedAt) = MONTH(CURRENT_DATE) " +
            "AND o.store.id = :storeId " +
            "AND o.orderStatus.statusName = 'DONE'")
    Long getTotalOrdersThisMonth(@Param("storeId") Long storeId);

    @Query("SELECT NEW com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreRevenueByDateRangeResponse(" +
            "MONTH(o.updatedAt), YEAR(o.updatedAt), SUM(o.totalPrice)) " +
            "FROM Order o " +
            "WHERE o.updatedAt BETWEEN :startDate AND :endDate " +
            "AND o.orderStatus.statusName = 'DONE' " +
            "AND o.store.id = :storeId " +
            "GROUP BY MONTH(o.updatedAt), YEAR(o.updatedAt) " +
            "ORDER BY YEAR(o.updatedAt), MONTH(o.updatedAt)")
    List<StoreRevenueByDateRangeResponse> getRevenueByDateRange(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT NEW com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreDailyRevenueResponse(" +
            "DAY(o.updatedAt), MONTH(o.updatedAt), YEAR(o.updatedAt), SUM(o.totalPrice)) " +
            "FROM Order o " +
            "WHERE MONTH(o.updatedAt) = :month " +
            "AND YEAR(o.updatedAt) = :year " +
            "AND o.orderStatus.statusName = 'DONE' " +
            "AND o.store.id = :storeId " +
            "GROUP BY DAY(o.updatedAt), MONTH(o.updatedAt), YEAR(o.updatedAt) " +
            "ORDER BY DAY(o.updatedAt)")
    List<StoreDailyRevenueResponse> getDailyRevenueByMonthAndYear(
            @Param("storeId") Long storeId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

}
