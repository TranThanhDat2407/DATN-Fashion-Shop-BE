package com.example.DATN_Fashion_Shop_BE.audit.entity;

import com.example.DATN_Fashion_Shop_BE.audit.entity.CustomInventoryRevesionEntity;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import jakarta.persistence.*;
import org.hibernate.envers.RevisionListener;
import org.springframework.boot.actuate.audit.listener.AuditListener;
import org.springframework.stereotype.Component;

@Component
public class CustomAuditListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof CustomInventoryRevesionEntity) {
            CustomInventoryRevesionEntity entity = (CustomInventoryRevesionEntity) revisionEntity;
            entity.setDeltaQuantity(0); // Mặc định là 0 nếu không có dữ liệu trước đó
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    @PrePersist
    @PreUpdate
    public void calculateDelta(CustomInventoryRevesionEntity entity) {
        if (entityManager != null) {
            Inventory previousInventory = entityManager.find(Inventory.class, entity.getId());
            if (previousInventory != null) {
                Integer previousQuantity = previousInventory.getQuantityInStock();
                Inventory currentInventory = entityManager.find(Inventory.class, entity.getId());

                if (currentInventory != null) {
                    entity.setDeltaQuantity(currentInventory.getQuantityInStock() - previousQuantity);
                }
            } else {
                entity.setDeltaQuantity(0); // Nếu không có bản ghi trước đó, set delta = 0
            }
        }
    }
}
