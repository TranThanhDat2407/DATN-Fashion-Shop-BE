package com.example.DATN_Fashion_Shop_BE.audit.entity;

import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@RevisionEntity(CustomAuditListener.class)
@Table(name = "inventories_aud")  //inventories_aud
public class CustomInventoryRevesionEntity extends DefaultRevisionEntity {

    @Column(name = "delta_quantity",nullable = true)
    private Integer deltaQuantity;



}
