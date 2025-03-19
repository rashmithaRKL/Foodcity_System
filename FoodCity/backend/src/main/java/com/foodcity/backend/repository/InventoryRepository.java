package com.foodcity.backend.repository;

import com.foodcity.backend.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {

    // Basic queries provided by MongoRepository
    
    Optional<Inventory> findByProductCode(String productCode);
    Optional<Inventory> findByBarcode(String barcode);
    boolean existsByProductCode(String productCode);
    boolean existsByBarcode(String barcode);

    // Category related queries
    List<Inventory> findByCategory(String category);
    Page<Inventory> findByCategory(String category, Pageable pageable);
    long countByCategory(String category);

    // Stock level queries
    List<Inventory> findByQuantityLessThanEqual(Integer threshold);
    List<Inventory> findByQuantityEquals(Integer quantity);
    List<Inventory> findByQuantityGreaterThan(Integer threshold);

    // Search queries
    Page<Inventory> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);
    
    List<Inventory> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description);

    // Expiry related queries
    List<Inventory> findByExpiryDateBefore(LocalDateTime date);
    List<Inventory> findByExpiryDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Location related queries
    List<Inventory> findByStorageLocation(String location);
    List<Inventory> findByStorageLocationStartingWith(String locationPrefix);

    // Supplier related queries
    List<Inventory> findBySupplierId(String supplierId);
    Page<Inventory> findBySupplierId(String supplierId, Pageable pageable);

    // Custom queries

    // Find items below reorder point
    @Query("{ 'quantity': { $lte: '$reorderPoint' } }")
    List<Inventory> findItemsNeedingReorder();

    // Find items with quality issues
    @Query("{ 'qualityStatus': { $ne: 'GOOD' } }")
    List<Inventory> findItemsWithQualityIssues();

    // Find items with recent movements
    @Query("{ 'lastMovementDate': { $gte: ?0 } }")
    List<Inventory> findItemsWithRecentMovements(LocalDateTime since);

    // Find items by multiple categories
    List<Inventory> findByCategoryIn(List<String> categories);

    // Find items by status
    List<Inventory> findByStatus(Inventory.Status status);

    // Find items with pending quality checks
    @Query("{ 'lastQualityCheckDate': { $lt: ?0 } }")
    List<Inventory> findItemsNeedingQualityCheck(LocalDateTime thresholdDate);

    // Find items with specific batch numbers
    List<Inventory> findByBatchNumber(String batchNumber);

    // Find items with price changes
    @Query("{ 'priceHistory': { $exists: true, $ne: [] } }")
    List<Inventory> findItemsWithPriceChanges();

    // Find items by cost range
    List<Inventory> findByUnitCostBetween(Double minCost, Double maxCost);

    // Find items with specific tags
    @Query("{ 'tags': { $in: ?0 } }")
    List<Inventory> findByTags(List<String> tags);

    // Find items requiring attention
    @Query("{ $or: [ " +
           "{ 'quantity': { $lte: '$reorderPoint' } }, " +
           "{ 'expiryDate': { $lte: ?0 } }, " +
           "{ 'qualityStatus': { $ne: 'GOOD' } } " +
           "] }")
    List<Inventory> findItemsRequiringAttention(LocalDateTime expiryThreshold);

    // Find items by movement type
    @Query("{ 'movements': { $elemMatch: { 'type': ?0 } } }")
    List<Inventory> findByMovementType(String movementType);

    // Find items with adjustments
    @Query("{ 'adjustments': { $exists: true, $ne: [] } }")
    List<Inventory> findItemsWithAdjustments();

    // Find items by supplier and status
    List<Inventory> findBySupplierIdAndStatus(String supplierId, Inventory.Status status);

    // Find items by location and category
    List<Inventory> findByStorageLocationAndCategory(String location, String category);

    // Count items by status
    long countByStatus(Inventory.Status status);

    // Find items with specific conditions
    @Query("{ 'condition': ?0 }")
    List<Inventory> findByCondition(String condition);

    // Find items by last update date
    List<Inventory> findByLastUpdatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Delete expired items
    void deleteByExpiryDateBefore(LocalDateTime date);

    // Find items by multiple suppliers
    List<Inventory> findBySupplierIdIn(List<String> supplierIds);

    // Find items by storage type
    List<Inventory> findByStorageType(String storageType);

    // Find items with specific handling requirements
    List<Inventory> findBySpecialHandlingRequired(boolean specialHandlingRequired);

    // Find items by temperature range
    @Query("{ 'storageTemperature': { $gte: ?0, $lte: ?1 } }")
    List<Inventory> findByTemperatureRange(double minTemp, double maxTemp);
}