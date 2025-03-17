package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class InventoryTransfer extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @NotAudited
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @NotAudited
    private Store store;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private String message;

    private Boolean isReturn = false;

    @OneToMany(mappedBy = "inventoryTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryTransferItem> transferItems = new ArrayList<>();

}
