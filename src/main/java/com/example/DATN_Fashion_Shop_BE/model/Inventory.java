package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Inventory extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Store store;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;

    @Column(name = "delta_quantity")
    private Integer deltaQuantity;

    @Transient
    private Integer previousQuantity;

    @PostLoad
    public void storePreviousQuantity() {
        this.previousQuantity = this.quantityInStock;
    }

    @PreUpdate
    public void preDeltaUpdate() {
        if (previousQuantity != null) {
            this.deltaQuantity = this.quantityInStock - previousQuantity;
        }
    }
}
