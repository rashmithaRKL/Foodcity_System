package com.foodcity.backend.service;

import com.foodcity.backend.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InventoryService {
    
    Inventory addInventoryItem(Inventory item);
    
    Inventory getInventoryItemById(String id);
    
    Page<Inventory> getAllInventoryItems(String search, String category, Boolean lowStock, Pageable pageable);
    
    Inventory updateInventoryItem(String id, Inventory item);
    
    void deleteInventoryItem(String id);
    
    Inventory updateStock(String id, Integer quantity, String reason);
    
    List<Inventory> getLowStockItems();
    
    List<Inventory> getExpiringItems(int days);
    
    Map<String, Object> performStockTake(Map<String, Integer> stockCount);
    
    Map<String, Object> getInventoryStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Inventory> generateReorderList();
    
    Map<String, Object> getInventoryMovement(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Inventory> adjustInventory(List<Map<String, Object>> adjustments);
    
    List<Map<String, Object>> getAuditTrail(String itemId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Inventory> batchUpdateInventory(List<Inventory> items);
    
    Map<String, Object> getInventoryValuation();
    
    // Additional business methods
    
    boolean existsByProductCode(String productCode);
    
    boolean existsByBarcode(String barcode);
    
    List<Inventory> getItemsByCategory(String category);
    
    Map<String, Integer> getStockLevels();
    
    void processStockAdjustment(String id, int quantity, String reason);
    
    Map<String, Object> getCategoryMetrics();
    
    List<Inventory> getOutOfStockItems();
    
    Map<String, Object> getStockTurnoverRate();
    
    void updateReorderPoints(Map<String, Integer> reorderPoints);
    
    List<Map<String, Object>> getInventoryAlerts();
    
    Map<String, Object> getSupplierInventoryMetrics(String supplierId);
    
    void processDelivery(String deliveryId, Map<String, Object> deliveryDetails);
    
    Map<String, Object> getWastageReport(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Inventory> getItemsNeedingRestock();
    
    Map<String, Object> getStorageMetrics();
    
    void updateStorageLocations(Map<String, String> locationUpdates);
    
    List<Map<String, Object>> getInventoryForecast();
    
    void processInventoryReturn(String returnId, Map<String, Object> returnDetails);
    
    Map<String, Object> getQualityMetrics();
    
    void updateQualityStatus(String id, String status, String notes);
}