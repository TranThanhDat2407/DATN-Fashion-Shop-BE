package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.service.InventoryService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/inventory")
@AllArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/store/{storeId}/history")
    public List<Object[]> getInventoryHistoryByStore(@PathVariable Long storeId) {
        return inventoryService.getInventoryHistoryByStore(storeId);
    }
}
