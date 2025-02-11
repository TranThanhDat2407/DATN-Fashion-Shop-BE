package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductVariantIdAndWarehouseNotNull(Long productVariantId);

    @Query("SELECT COALESCE(SUM(i.quantityInStock), 0) FROM Inventory i " +
            "WHERE i.productVariant.id = :variantId " +
            "AND i.warehouse IS NOT NULL")
    Integer getStockByVariant(@Param("variantId") Long variantId);
}
