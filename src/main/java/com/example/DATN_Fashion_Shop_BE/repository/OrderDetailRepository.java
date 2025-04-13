package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top10Products;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.TopProductsInStoreResponse;
import com.example.DATN_Fashion_Shop_BE.model.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);

    @Query("SELECT od FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.store.id = :storeId " +
            "AND o.orderStatus.statusName = 'DONE' " +
            "ORDER BY od.updatedAt DESC")
    Page<OrderDetail> findLatestDoneOrderDetails(@Param("storeId") Long storeId, Pageable pageable);


    @Query("SELECT new com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.TopProductsInStoreResponse(" +
            "od.productVariant.id, " +
            "(SELECT pt.name FROM ProductsTranslation pt " +
            "WHERE pt.product = od.productVariant.product AND pt.language.code = :languageCode), " +
            "od.productVariant.colorValue.valueName, " +
            "od.productVariant.colorValue.valueImg, " +
            "od.productVariant.sizeValue.valueName, " +
            "(SELECT pm.mediaUrl FROM ProductMedia pm " +
            "WHERE pm.product = od.productVariant.product AND pm.colorValue.id = od.productVariant.colorValue.id " +
            "ORDER BY pm.id ASC LIMIT 1), " +
            "SUM(od.quantity), " +  // Tổng số lượng đã bán
            "SUM(od.totalPrice)) " + // Tổng doanh thu
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.orderStatus.statusName = 'DONE' AND o.store.id = :storeId " +
            "GROUP BY od.productVariant.id, " +
            "od.productVariant.colorValue.valueName, od.productVariant.colorValue.valueImg, " +
            "od.productVariant.sizeValue.valueName, " +
            "od.productVariant.product, od.productVariant.colorValue.id " +
            "ORDER BY SUM(od.quantity) DESC")
    Page<TopProductsInStoreResponse> findTopProductsByStoreId(
            @Param("storeId") Long storeId,
            @Param("languageCode") String languageCode,
            Pageable pageable
            );



    @Query("""
        SELECT new com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top10Products(
            od.productVariant.id,
            (SELECT pt.name FROM ProductsTranslation pt
             WHERE pt.product = od.productVariant.product AND pt.language.code = :languageCode),
            od.productVariant.colorValue.valueName,
            od.productVariant.colorValue.valueImg,
            od.productVariant.sizeValue.valueName,
            SUM(od.quantity),
            SUM(od.totalPrice),
            od.productVariant.product.id,
            od.productVariant.colorValue.id
        )
        FROM OrderDetail od
        JOIN od.order o
        WHERE o.orderStatus.statusName = 'DONE'
        GROUP BY od.productVariant.id,
                 od.productVariant.colorValue.valueName,
                 od.productVariant.colorValue.valueImg,
                 od.productVariant.sizeValue.valueName,
                 od.productVariant.product.id,
                 od.productVariant.colorValue.id
        ORDER BY SUM(od.quantity) DESC
    """)
    List<Top10Products> findTopSellingProducts(
            @Param("languageCode") String languageCode
    );



}
