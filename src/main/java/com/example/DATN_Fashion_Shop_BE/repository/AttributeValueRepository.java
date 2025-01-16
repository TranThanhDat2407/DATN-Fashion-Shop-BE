package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    @Query("SELECT av FROM AttributeValue av " +
            "JOIN av.attribute a " +
            "JOIN ProductVariant pv ON pv.colorValue.id = av.id " +
            "WHERE pv.product.id = :productId AND a.name = 'Color'")
    List<AttributeValue> findColorsByProductId(@Param("productId") Long productId);

    @Query("SELECT av FROM AttributeValue av " +
            "JOIN av.attribute a " +
            "JOIN ProductVariant pv ON pv.sizeValue.id = av.id " +
            "WHERE pv.product.id = :productId AND a.name = 'Size'")
    List<AttributeValue> findSizesByProductId(@Param("productId") Long productId);
}
