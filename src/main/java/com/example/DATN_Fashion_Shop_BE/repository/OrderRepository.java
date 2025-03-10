package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Cart;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(Long userId, Pageable pageable);

   Optional<Order>findById(Long id);

    Page<Order> findByOrderStatus_StatusName(String statusName, Pageable pageable);

    // Lọc theo địa chỉ
    Page<Order> findByShippingAddressContainingIgnoreCase(String shippingAddress, Pageable pageable);

    // Lọc theo khoảng giá
    Page<Order> findByTotalPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // Lọc theo ngày tạo
    Page<Order> findByCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    // Lọc theo ngày cập nhật
    Page<Order> findByUpdatedAtBetween(LocalDateTime updateFromDate, LocalDateTime updateToDate, Pageable pageable);


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
}
