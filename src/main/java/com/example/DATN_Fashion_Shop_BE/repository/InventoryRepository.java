package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductVariantIdAndWarehouseNotNull(Long productVariantId);
    List<Inventory> findByProductVariantIdAndStoreNotNull(Long productVariantId);

    @Query("SELECT COALESCE(SUM(i.quantityInStock), 0) FROM Inventory i " +
            "WHERE i.productVariant.id = :variantId " +
            "AND i.warehouse IS NOT NULL")
    Integer getStockByVariant(@Param("variantId") Long variantId);

    @Query("""
        SELECT i.quantityInStock 
        FROM Inventory i 
        WHERE i.productVariant.product.id = :productId 
          AND i.productVariant.colorValue.id = :colorId 
          AND i.productVariant.sizeValue.id = :sizeId 
          AND i.store.id = :storeId
    """)
    Optional<Integer> findQuantityInStockStoreId(@Param("productId") Long productId,
                                                 @Param("colorId") Long colorId,
                                                 @Param("sizeId") Long sizeId,
                                                 @Param("storeId") Long storeId);
}
