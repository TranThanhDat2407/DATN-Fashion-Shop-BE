package com.example.DATN_Fashion_Shop_BE.audit.entity;

import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "inventories_aud")  //inventories_aud
public class CustomInventoryRevesionEntity extends DefaultRevisionEntity {

    @Column(name = "delta_quantity",nullable = true)
    private Integer deltaQuantity;

    @Setter
    @Transient // Không lưu vào DB, chỉ dùng để lấy dữ liệu
    private static EntityManager entityManager;

    @PostPersist // Tự động chạy sau khi entity được lưu
    public void calculateDelta() {
        if (entityManager != null) {
            // Tìm lịch sử trước đó của Inventory
            Inventory previousInventory = entityManager.find(Inventory.class, this.getId());
            if (previousInventory != null) {
                Integer previousQuantity = previousInventory.getQuantityInStock();
                Inventory currentInventory = entityManager.find(Inventory.class, this.getId());

                if (currentInventory != null) {
                    this.deltaQuantity = currentInventory.getQuantityInStock() - previousQuantity;
                }
            }

        }
    }

}
