package com.foodcity.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "inventory")
public class Inventory {
    @Id
    private String id;

    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Current quantity is required")
    @Min(value = 0, message = "Current quantity cannot be negative")
    private Integer currentQuantity;

    @NotNull(message = "Minimum quantity is required")
    @Min(value = 0, message = "Minimum quantity cannot be negative")
    private Integer minimumQuantity;

    @NotNull(message = "Reorder point is required")
    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint;

    private String unit; // kg, pieces, packets, etc.
    private String location; // warehouse location
    private List<BatchInfo> batches;
    private List<StockMovement> movements;
    private List<SupplierInfo> suppliers;

    // Stock Status
    private StockStatus status;
    private boolean needsReorder;
    private LocalDateTime lastRestockDate;
    private LocalDateTime nextScheduledRestock;

    // Quality Control
    private List<QualityCheck> qualityChecks;
    private boolean qualityApproved;
    private LocalDateTime lastQualityCheckDate;

    // Tracking
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdated;
    private String lastUpdatedBy;

    @Data
    public static class BatchInfo {
        private String batchNumber;
        private Integer quantity;
        private LocalDateTime manufacturingDate;
        private LocalDateTime expiryDate;
        private BigDecimal costPrice;
        private String supplierReference;
        private BatchStatus status;
    }

    @Data
    public static class StockMovement {
        private MovementType type;
        private Integer quantity;
        private String referenceNumber; // Order ID or Supply ID
        private String reason;
        private LocalDateTime date;
        private String handledBy;
        private String notes;
    }

    @Data
    public static class SupplierInfo {
        private String supplierId;
        private String supplierName;
        private BigDecimal lastPurchasePrice;
        private Integer leadTime; // in days
        private Integer minimumOrderQuantity;
        private String preferredSupplier;
        private LocalDateTime lastSupplyDate;
    }

    @Data
    public static class QualityCheck {
        private String checkId;
        private QualityStatus status;
        private String checkedBy;
        private LocalDateTime checkDate;
        private String notes;
        private List<String> issues;
        private String batchNumber;
    }

    public enum StockStatus {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK,
        DISCONTINUED,
        ON_HOLD
    }

    public enum BatchStatus {
        ACTIVE,
        EXPIRED,
        QUARANTINE,
        CONSUMED,
        DAMAGED
    }

    public enum MovementType {
        PURCHASE,
        SALE,
        RETURN,
        DAMAGE,
        ADJUSTMENT,
        TRANSFER
    }

    public enum QualityStatus {
        PASSED,
        FAILED,
        PENDING,
        UNDER_REVIEW
    }
}