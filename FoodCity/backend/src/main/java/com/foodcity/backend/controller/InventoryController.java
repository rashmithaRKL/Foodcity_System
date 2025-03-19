package com.foodcity.backend.controller;

import com.foodcity.backend.model.Inventory;
import com.foodcity.backend.payload.ApiResponse;
import com.foodcity.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse> addInventoryItem(@Valid @RequestBody Inventory item) {
        Inventory savedItem = inventoryService.addInventoryItem(item);
        
        // Notify about new inventory item
        messagingTemplate.convertAndSend("/topic/inventory/new", savedItem);
        
        return ResponseEntity.ok(new ApiResponse(true, "Inventory item added successfully", savedItem));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'CASHIER')")
    public ResponseEntity<Inventory> getInventoryItemById(@PathVariable String id) {
        Inventory item = inventoryService.getInventoryItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'CASHIER')")
    public ResponseEntity<Page<Inventory>> getAllInventoryItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock,
            Pageable pageable) {
        Page<Inventory> items = inventoryService.getAllInventoryItems(search, category, lowStock, pageable);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse> updateInventoryItem(
            @PathVariable String id,
            @Valid @RequestBody Inventory item) {
        Inventory updatedItem = inventoryService.updateInventoryItem(id, item);
        
        // Notify about inventory update
        messagingTemplate.convertAndSend("/topic/inventory/" + id, updatedItem);
        
        return ResponseEntity.ok(new ApiResponse(true, "Inventory item updated successfully", updatedItem));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteInventoryItem(@PathVariable String id) {
        inventoryService.deleteInventoryItem(id);
        
        // Notify about inventory deletion
        messagingTemplate.convertAndSend("/topic/inventory/deleted", id);
        
        return ResponseEntity.ok(new ApiResponse(true, "Inventory item deleted successfully"));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse> updateStock(
            @PathVariable String id,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String reason) {
        Inventory item = inventoryService.updateStock(id, quantity, reason);
        return ResponseEntity.ok(new ApiResponse(true, "Stock updated successfully", item));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        List<Inventory> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<List<Inventory>> getExpiringItems(
            @RequestParam(defaultValue = "30") int days) {
        List<Inventory> items = inventoryService.getExpiringItems(days);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/stock-take")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse> performStockTake(@RequestBody Map<String, Integer> stockCount) {
        Map<String, Object> result = inventoryService.performStockTake(stockCount);
        return ResponseEntity.ok(new ApiResponse(true, "Stock take completed", result));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getInventoryStatistics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> statistics = inventoryService.getInventoryStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse> generateReorderList() {
        List<Inventory> reorderList = inventoryService.generateReorderList();
        return ResponseEntity.ok(new ApiResponse(true, "Reorder list generated", reorderList));
    }

    @GetMapping("/movement")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> getInventoryMovement(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> movement = inventoryService.getInventoryMovement(startDate, endDate);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> adjustInventory(
            @RequestBody List<Map<String, Object>> adjustments) {
        List<Inventory> adjusted = inventoryService.adjustInventory(adjustments);
        return ResponseEntity.ok(new ApiResponse(true, "Inventory adjusted successfully", adjusted));
    }

    @GetMapping("/audit-trail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAuditTrail(
            @RequestParam String itemId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<Map<String, Object>> auditTrail = inventoryService.getAuditTrail(itemId, startDate, endDate);
        return ResponseEntity.ok(auditTrail);
    }

    @PostMapping("/batch-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> batchUpdateInventory(
            @RequestBody List<Inventory> items) {
        List<Inventory> updated = inventoryService.batchUpdateInventory(items);
        return ResponseEntity.ok(new ApiResponse(true, "Batch update completed", updated));
    }

    @GetMapping("/valuation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getInventoryValuation() {
        Map<String, Object> valuation = inventoryService.getInventoryValuation();
        return ResponseEntity.ok(valuation);
    }
}