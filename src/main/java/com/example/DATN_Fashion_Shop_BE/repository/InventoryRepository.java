package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.InventoryStatistics;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT COALESCE(SUM(i.quantityInStock), 0) FROM Inventory i WHERE i.store.id = :storeId AND i.productVariant.id = :variantId")
    Integer findQuantityInStockByStoreAndVariant(@Param("storeId") Long storeId, @Param("variantId") Long variantId);


    Page<Inventory> findByStoreIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Translations_NameContainingIgnoreCaseAndProductVariant_Product_Categories_IdIn(
            Long storeId, String languageCode, String productName, List<Long> categoryIds, Pageable pageable);

    Page<Inventory> findByStoreIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Categories_IdIn(
            Long storeId, String languageCode, List<Long> categoryIds, Pageable pageable);

    Page<Inventory> findByStoreIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Translations_NameContainingIgnoreCase(
            Long storeId, String languageCode, String productName, Pageable pageable);

    Page<Inventory> findByStoreIdAndProductVariant_Product_Translations_LanguageCode(
            Long storeId, String languageCode, Pageable pageable);

    Optional<Inventory> findByStoreIdAndProductVariantId(Long storeId, Long productVariantId);

    List<Inventory> findByStoreId(Long storeId);




    @Query("""
    SELECT new com.example.DATN_Fashion_Shop_BE.dto.response.revenue.InventoryStatistics(
        pv.id, 
        pt.name, 
        avColor.valueName, 
        avColor.valueImg, 
        avSize.valueName, 
        pm.mediaUrl, 
        SUM(i.quantityInStock)
    )
    FROM Inventory i
    JOIN i.productVariant pv
    JOIN pv.product p
    JOIN p.translations pt
    LEFT JOIN pv.colorValue avColor
    LEFT JOIN pv.sizeValue avSize
    LEFT JOIN ProductMedia pm ON pm.product = p AND pm.colorValue.id = pv.colorValue.id
    WHERE i.store.id = :storeId
    AND pt.language.code = 'vi'  
    AND (:productName IS NULL OR LOWER(pt.name) LIKE LOWER(CONCAT('%', :productName, '%')))
    AND (:color IS NULL OR LOWER(avColor.valueName) LIKE LOWER(CONCAT('%', :color, '%')))
    AND (:size IS NULL OR LOWER(avSize.valueName) LIKE LOWER(CONCAT('%', :size, '%')))
    GROUP BY pv.id, pt.name, avColor.valueName, avColor.valueImg, avSize.valueName, pm.mediaUrl
    ORDER BY pv.id ASC
""")
    Page<InventoryStatistics> findInventoryByStore(
            @Param("storeId") Long storeId,
            @Param("productName") String productName,
            @Param("color") String color,
            @Param("size") String size,
            Pageable pageable);





}
