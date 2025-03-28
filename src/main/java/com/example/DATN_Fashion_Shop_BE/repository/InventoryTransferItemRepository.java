package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.InventoryTransfer;
import com.example.DATN_Fashion_Shop_BE.model.InventoryTransferItem;
import com.example.DATN_Fashion_Shop_BE.model.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InventoryTransferItemRepository extends JpaRepository<InventoryTransferItem, Long> {
    List<InventoryTransferItem> findByInventoryTransferId(Long inventoryTransferId);
}
