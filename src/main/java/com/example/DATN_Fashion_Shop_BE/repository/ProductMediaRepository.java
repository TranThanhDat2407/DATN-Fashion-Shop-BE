package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductMedia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {
    List<ProductMedia> findByProductId(long productId);
    List<ProductMedia> findByProductIdAndColorValueId(Long productId, Long colorId);
    Optional<ProductMedia> findFirstByProduct_IdAndColorValue_IdOrderByIdAsc(Long productId, Long colorValueId);

}
