package com.foodcity.backend.service.impl;

import com.foodcity.backend.exception.ResourceNotFoundException;
import com.foodcity.backend.model.Inventory;
import com.foodcity.backend.repository.InventoryRepository;
import com.foodcity.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public Inventory addInventoryItem(Inventory item) {
        validateNewItem(item);
        item.setLastUpdated(LocalDateTime.now());
        return inventoryRepository.save(item);
    }

    @Override
    public Inventory getInventoryItemById(String id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", id));
    }

    @Override
    public Page<Inventory> getAllInventoryItems(String search, String category, Boolean lowStock, Pageable pageable) {
        if (search != null) {
            return inventoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search, search, pageable);
        } else if (category != null) {
            return inventoryRepository.findByCategory(category, pageable);
        } else if (Boolean.TRUE.equals(lowStock)) {
            return Page.empty(); // Implement custom logic for low stock items
        }
        return inventoryRepository.findAll(pageable);
    }

    @Override
    public Inventory updateInventoryItem(String id, Inventory itemDetails) {
        Inventory item = getInventoryItemById(id);
        updateItemFields(item, itemDetails);
        return inventoryRepository.save(item);
    }

    @Override
    public void deleteInventoryItem(String id) {
        Inventory item = getInventoryItemById(id);
        item.setStatus(Inventory.Status.INACTIVE);
        inventoryRepository.save(item);
    }

    @Override
    public Inventory updateStock(String id, Integer quantity, String reason) {
        Inventory item = getInventoryItemById(id);
        
        Inventory.StockMovement movement = new Inventory.StockMovement();
        movement.setDate(LocalDateTime.now());
        movement.setQuantity(quantity - item.getQuantity());
        movement.setReason(reason);
        
        item.setQuantity(quantity);
        item.getMovements().add(movement);
        item.setLastUpdated(LocalDateTime.now());
        
        return inventoryRepository.save(item);
    }

    @Override
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findItemsNeedingReorder();
    }

    @Override
    public List<Inventory> getExpiringItems(int days) {
        LocalDateTime thresholdDate = LocalDateTime.now().plusDays(days);
        return inventoryRepository.findByExpiryDateBefore(thresholdDate);
    }

    @Override
    public Map<String, Object> performStockTake(Map<String, Integer> stockCount) {
        Map<String, Object> result = new HashMap<>();
        List<Inventory.StockDiscrepancy> discrepancies = new ArrayList<>();
        
        stockCount.forEach((id, count) -> {
            try {
                Inventory item = getInventoryItemById(id);
                if (!Objects.equals(item.getQuantity(), count)) {
                    discrepancies.add(createDiscrepancy(item, count));
                    updateStock(id, count, "Stock take adjustment");
                }
            } catch (ResourceNotFoundException e) {
                // Log the error
            }
        });
        
        result.put("discrepancies", discrepancies);
        result.put("completedDate", LocalDateTime.now());
        return result;
    }

    @Override
    public Map<String, Object> getInventoryStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Inventory> items = inventoryRepository.findByLastUpdatedBetween(startDate, endDate);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalItems", items.size());
        statistics.put("totalValue", calculateTotalValue(items));
        statistics.put("lowStockItems", items.stream()
                .filter(item -> item.getQuantity() <= item.getReorderPoint())
                .count());
        statistics.put("categoryDistribution", getCategoryDistribution(items));
        
        return statistics;
    }

    @Override
    public List<Inventory> generateReorderList() {
        return inventoryRepository.findItemsNeedingReorder();
    }

    @Override
    public Map<String, Object> getInventoryMovement(LocalDateTime startDate, LocalDateTime endDate) {
        List<Inventory> items = inventoryRepository.findItemsWithRecentMovements(startDate);
        
        Map<String, Object> movement = new HashMap<>();
        movement.put("totalMovements", countTotalMovements(items, startDate, endDate));
        movement.put("movementsByType", getMovementsByType(items, startDate, endDate));
        
        return movement;
    }

    @Override
    public List<Inventory> adjustInventory(List<Map<String, Object>> adjustments) {
        return adjustments.stream()
                .map(this::processAdjustment)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAuditTrail(String itemId, LocalDateTime startDate, LocalDateTime endDate) {
        Inventory item = getInventoryItemById(itemId);
        return item.getMovements().stream()
                .filter(movement -> isMovementInDateRange(movement, startDate, endDate))
                .map(this::convertMovementToMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> batchUpdateInventory(List<Inventory> items) {
        return inventoryRepository.saveAll(items);
    }

    @Override
    public Map<String, Object> getInventoryValuation() {
        List<Inventory> items = inventoryRepository.findAll();
        
        Map<String, Object> valuation = new HashMap<>();
        valuation.put("totalValue", calculateTotalValue(items));
        valuation.put("valuationByCategory", getValuationByCategory(items));
        
        return valuation;
    }

    @Override
    public boolean existsByProductCode(String productCode) {
        return inventoryRepository.existsByProductCode(productCode);
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return inventoryRepository.existsByBarcode(barcode);
    }

    @Override
    public List<Inventory> getItemsByCategory(String category) {
        return inventoryRepository.findByCategory(category);
    }

    @Override
    public Map<String, Integer> getStockLevels() {
        List<Inventory> items = inventoryRepository.findAll();
        return items.stream()
                .collect(Collectors.toMap(
                        Inventory::getProductCode,
                        Inventory::getQuantity
                ));
    }

    @Override
    public void processStockAdjustment(String id, int quantity, String reason) {
        updateStock(id, quantity, reason);
    }

    @Override
    public Map<String, Object> getCategoryMetrics() {
        List<Inventory> items = inventoryRepository.findAll();
        return calculateCategoryMetrics(items);
    }

    @Override
    public List<Inventory> getOutOfStockItems() {
        return inventoryRepository.findByQuantityEquals(0);
    }

    @Override
    public Map<String, Object> getStockTurnoverRate() {
        // Implementation for stock turnover rate calculation
        return new HashMap<>();
    }

    @Override
    public void updateReorderPoints(Map<String, Integer> reorderPoints) {
        reorderPoints.forEach((id, point) -> {
            Inventory item = getInventoryItemById(id);
            item.setReorderPoint(point);
            inventoryRepository.save(item);
        });
    }

    @Override
    public List<Map<String, Object>> getInventoryAlerts() {
        List<Inventory> items = inventoryRepository.findItemsRequiringAttention(LocalDateTime.now());
        return generateAlerts(items);
    }

    @Override
    public Map<String, Object> getSupplierInventoryMetrics(String supplierId) {
        List<Inventory> items = inventoryRepository.findBySupplierId(supplierId);
        return calculateSupplierMetrics(items);
    }

    @Override
    public void processDelivery(String deliveryId, Map<String, Object> deliveryDetails) {
        // Implementation for processing deliveries
    }

    @Override
    public Map<String, Object> getWastageReport(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for wastage reporting
        return new HashMap<>();
    }

    @Override
    public List<Inventory> getItemsNeedingRestock() {
        return inventoryRepository.findItemsNeedingReorder();
    }

    @Override
    public Map<String, Object> getStorageMetrics() {
        // Implementation for storage metrics
        return new HashMap<>();
    }

    @Override
    public void updateStorageLocations(Map<String, String> locationUpdates) {
        locationUpdates.forEach((id, location) -> {
            Inventory item = getInventoryItemById(id);
            item.setStorageLocation(location);
            inventoryRepository.save(item);
        });
    }

    @Override
    public List<Map<String, Object>> getInventoryForecast() {
        // Implementation for inventory forecasting
        return new ArrayList<>();
    }

    @Override
    public void processInventoryReturn(String returnId, Map<String, Object> returnDetails) {
        // Implementation for processing returns
    }

    @Override
    public Map<String, Object> getQualityMetrics() {
        List<Inventory> items = inventoryRepository.findItemsWithQualityIssues();
        return calculateQualityMetrics(items);
    }

    @Override
    public void updateQualityStatus(String id, String status, String notes) {
        Inventory item = getInventoryItemById(id);
        item.setQualityStatus(status);
        item.setQualityNotes(notes);
        inventoryRepository.save(item);
    }

    // Private helper methods

    private void validateNewItem(Inventory item) {
        if (inventoryRepository.existsByProductCode(item.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists");
        }
        if (inventoryRepository.existsByBarcode(item.getBarcode())) {
            throw new IllegalArgumentException("Barcode already exists");
        }
    }

    private void updateItemFields(Inventory item, Inventory itemDetails) {
        item.setName(itemDetails.getName());
        item.setDescription(itemDetails.getDescription());
        item.setCategory(itemDetails.getCategory());
        item.setUnitCost(itemDetails.getUnitCost());
        item.setReorderPoint(itemDetails.getReorderPoint());
        item.setStorageLocation(itemDetails.getStorageLocation());
        item.setLastUpdated(LocalDateTime.now());
    }

    private Inventory.StockDiscrepancy createDiscrepancy(Inventory item, int actualCount) {
        Inventory.StockDiscrepancy discrepancy = new Inventory.StockDiscrepancy();
        discrepancy.setDate(LocalDateTime.now());
        discrepancy.setExpectedQuantity(item.getQuantity());
        discrepancy.setActualQuantity(actualCount);
        discrepancy.setDifference(actualCount - item.getQuantity());
        return discrepancy;
    }

    private double calculateTotalValue(List<Inventory> items) {
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitCost().doubleValue())
                .sum();
    }

    private Map<String, Long> getCategoryDistribution(List<Inventory> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getCategory,
                        Collectors.counting()
                ));
    }

    private int countTotalMovements(List<Inventory> items, LocalDateTime startDate, LocalDateTime endDate) {
        return items.stream()
                .mapToInt(item -> (int) item.getMovements().stream()
                        .filter(movement -> isMovementInDateRange(movement, startDate, endDate))
                        .count())
                .sum();
    }

    private Map<String, Integer> getMovementsByType(List<Inventory> items, LocalDateTime startDate, LocalDateTime endDate) {
        return items.stream()
                .flatMap(item -> item.getMovements().stream())
                .filter(movement -> isMovementInDateRange(movement, startDate, endDate))
                .collect(Collectors.groupingBy(
                        Inventory.StockMovement::getReason,
                        Collectors.summingInt(movement -> Math.abs(movement.getQuantity()))
                ));
    }

    private Inventory processAdjustment(Map<String, Object> adjustment) {
        String id = (String) adjustment.get("id");
        Integer quantity = (Integer) adjustment.get("quantity");
        String reason = (String) adjustment.get("reason");
        return updateStock(id, quantity, reason);
    }

    private boolean isMovementInDateRange(Inventory.StockMovement movement, LocalDateTime startDate, LocalDateTime endDate) {
        return !movement.getDate().isBefore(startDate) && !movement.getDate().isAfter(endDate);
    }

    private Map<String, Object> convertMovementToMap(Inventory.StockMovement movement) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", movement.getDate());
        map.put("quantity", movement.getQuantity());
        map.put("reason", movement.getReason());
        return map;
    }

    private Map<String, Double> getValuationByCategory(List<Inventory> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getCategory,
                        Collectors.summingDouble(item -> 
                                item.getQuantity() * item.getUnitCost().doubleValue())
                ));
    }

    private Map<String, Object> calculateCategoryMetrics(List<Inventory> items) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("categoryCounts", getCategoryDistribution(items));
        metrics.put("categoryValues", getValuationByCategory(items));
        return metrics;
    }

    private List<Map<String, Object>> generateAlerts(List<Inventory> items) {
        return items.stream()
                .map(item -> {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("itemId", item.getId());
                    alert.put("productCode", item.getProductCode());
                    alert.put("alertType", determineAlertType(item));
                    alert.put("message", generateAlertMessage(item));
                    return alert;
                })
                .collect(Collectors.toList());
    }

    private String determineAlertType(Inventory item) {
        if (item.getQuantity() <= item.getReorderPoint()) {
            return "LOW_STOCK";
        } else if (item.getExpiryDate() != null && 
                item.getExpiryDate().isBefore(LocalDateTime.now().plusDays(30))) {
            return "EXPIRING_SOON";
        }
        return "QUALITY_ISSUE";
    }

    private String generateAlertMessage(Inventory item) {
        switch (determineAlertType(item)) {
            case "LOW_STOCK":
                return "Stock level below reorder point";
            case "EXPIRING_SOON":
                return "Item expiring soon";
            default:
                return "Quality check required";
        }
    }

    private Map<String, Object> calculateSupplierMetrics(List<Inventory> items) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalItems", items.size());
        metrics.put("totalValue", calculateTotalValue(items));
        metrics.put("categoryBreakdown", getCategoryDistribution(items));
        return metrics;
    }

    private Map<String, Object> calculateQualityMetrics(List<Inventory> items) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalIssues", items.size());
        metrics.put("issuesByCategory", items.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getQualityStatus,
                        Collectors.counting()
                )));
        return metrics;
    }
}