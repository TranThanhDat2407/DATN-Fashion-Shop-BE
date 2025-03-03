package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import com.example.DATN_Fashion_Shop_BE.repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Object[]> getInventoryHistoryByStore(Long storeId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        return auditReader.createQuery()
                .forRevisionsOfEntity(Inventory.class, false, true)
                .add(AuditEntity.property("store_id").eq(storeId))
                .getResultList();
    }
}
