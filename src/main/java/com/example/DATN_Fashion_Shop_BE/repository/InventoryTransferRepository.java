package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.InventoryTransfer;
import com.example.DATN_Fashion_Shop_BE.model.TransferStatus;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.model.WishList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface InventoryTransferRepository extends JpaRepository<InventoryTransfer, Long> {
    @Query("SELECT it FROM InventoryTransfer it " +
            "WHERE :storeId IS NULL OR it.store.id = :storeId " +
            "AND (:status IS NULL OR it.status = :status) " +
            "AND (:isReturn IS NULL OR it.isReturn = :isReturn)")
    Page<InventoryTransfer> findByStoreIdAndStatusAndIsReturn(@Param("storeId") Long storeId,
                                                            @Param("status") TransferStatus status,
                                                            @Param("isReturn") Boolean isReturn,
                                                            Pageable pageable);
    @Query("SELECT it FROM InventoryTransfer it " +
            "WHERE (:storeId IS NULL OR it.store.id = :storeId) " +
            "AND (:status IS NULL OR it.status = :status) " +
            "AND (:isReturn IS NULL OR it.isReturn = :isReturn) " +
            "ORDER BY " +
            "CASE WHEN (it.status = 'PENDING' AND it.createdAt < :warningThreshold) THEN 0 ELSE 1 END")
    Page<InventoryTransfer> findWithWarningPriority(
            @Param("storeId") Long storeId,
            @Param("status") TransferStatus status,
            @Param("isReturn") Boolean isReturn,
            @Param("warningThreshold") LocalDateTime warningThreshold,
            Pageable pageable);
}
