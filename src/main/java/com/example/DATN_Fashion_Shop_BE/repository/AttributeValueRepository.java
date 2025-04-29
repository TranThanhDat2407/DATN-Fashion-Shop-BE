package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT av FROM AttributeValue av " +
            "WHERE av.attribute.name = 'Color' " +
            "AND LOWER(av.valueName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<AttributeValue> findAllColorsByName(@Param("name") String name, Pageable pageable);

    @Query("SELECT av FROM AttributeValue av " +
            "WHERE av.attribute.name = 'Size' " +
            "AND LOWER(av.valueName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<AttributeValue> findAndSizesName(@Param("name") String name, Pageable pageable);

    @Query("SELECT av FROM AttributeValue av JOIN av.attributeValuePatterns p " +
            "WHERE p.id = :patternId")
    List<AttributeValue> findByPatternId(@Param("patternId") Long patternId);

    @Query("SELECT av FROM AttributeValue av JOIN av.attributeValuePatterns p " +
            "WHERE p.id = :patternId AND LOWER(av.valueName) LIKE LOWER(CONCAT('%', :nameKeyword, '%'))")
    Page<AttributeValue> findAllByPatternIdAndName(@Param("patternId") Long patternId,
                                                   @Param("nameKeyword") String nameKeyword,
                                                   Pageable pageable);

}
