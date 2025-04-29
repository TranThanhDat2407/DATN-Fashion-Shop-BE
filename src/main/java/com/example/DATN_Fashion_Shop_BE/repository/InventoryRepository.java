package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.InventoryStatusResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.InventoryStatistics;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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






    Page<Inventory> findByWarehouseIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Translations_NameContainingIgnoreCaseAndProductVariant_Product_Categories_IdIn(
            Long warehouseId, String languageCode, String productName, List<Long> categoryIds, Pageable pageable);

    Page<Inventory> findByWarehouseIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Categories_IdIn(
            Long warehouseId, String languageCode, List<Long> categoryIds, Pageable pageable);

    Page<Inventory> findByWarehouseIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Translations_NameContainingIgnoreCase(
            Long warehouseId, String languageCode, String productName, Pageable pageable);

    Page<Inventory> findByWarehouseIdAndProductVariant_Product_Translations_LanguageCode(
            Long warehouseId, String languageCode, Pageable pageable);

    Optional<Inventory> findByWarehouseIdAndProductVariantId(Long warehouseId, Long productVariantId);

    @Query(value = """
    DECLARE @storeId BIGINT = :storeId;
    DECLARE @langCode NVARCHAR(10) = :langCode;

    WITH LastSale AS (
        SELECT
            od.product_variant_id AS productVariantId,
            MAX(o.updated_at) AS lastSoldDate
        FROM orders_details od
        JOIN orders o ON od.order_id = o.id
        JOIN order_status os ON os.id = o.status_id
        WHERE os.status_name = 'DONE'
          AND o.store_id = @storeId
        GROUP BY od.product_variant_id
    ),
    ProductTranslations AS (
        SELECT
            pt.product_id,
            pt.name as product_name,
            pt.language_id
        FROM products_translations pt
        WHERE pt.language_id IN (
            SELECT id FROM languages WHERE code IN (@langCode, 'en')
        )
    ),
    ProductMediaFirst AS (
        SELECT 
            pm.product_id,
            pm.color_value_id,
            pm.media_url,
            ROW_NUMBER() OVER(
                PARTITION BY pm.product_id, pm.color_value_id 
                ORDER BY pm.sort_order ASC, pm.id ASC
            ) as row_num
        FROM product_media pm
        WHERE pm.media_type = 'IMAGE'
    )
    SELECT
        pv.id as productVariantId,
        COALESCE(
            (SELECT TOP 1 pt.product_name FROM ProductTranslations pt
             WHERE pt.product_id = p.id AND pt.language_id =
                (SELECT id FROM languages WHERE code = @langCode)),
            (SELECT TOP 1 pt.product_name FROM ProductTranslations pt
             WHERE pt.product_id = p.id AND pt.language_id =
                (SELECT id FROM languages WHERE code = 'en')),
            CAST(p.id AS NVARCHAR(50))
        ) as productName,
          (SELECT TOP 1 pmf.media_url FROM ProductMediaFirst pmf 
         WHERE pmf.product_id = p.id AND pmf.color_value_id = color_val.id AND pmf.row_num = 1) as productImage,
        color_val.value_name as colorValue,
        color_val.value_img as colorImage,
        size_val.value_name as sizeValue,
        i.quantity_in_stock as quantityInStock,
        s.name as storeName,
        COALESCE(DATEDIFF(DAY, ls.lastSoldDate, GETDATE()), -1) as daysUnsold
    FROM inventories i
    JOIN product_variants pv ON i.product_variant_id = pv.id
    JOIN products p ON pv.product_id = p.id
    LEFT JOIN attributes_values color_val ON pv.color_value_id = color_val.id
    LEFT JOIN attributes_values size_val ON pv.size_value_id = size_val.id
    LEFT JOIN LastSale ls ON i.product_variant_id = ls.productVariantId
    LEFT JOIN store s ON i.store_id = s.id
    WHERE i.store_id = @storeId
    ORDER BY daysUnsold DESC, i.quantity_in_stock DESC
""", nativeQuery = true)
    List<InventoryStatusResponse> findUnsoldProductsByStore(
            @Param("storeId") Long storeId,
            @Param("langCode") String langCode);

}
