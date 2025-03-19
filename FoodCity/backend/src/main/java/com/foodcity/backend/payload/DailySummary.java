package com.foodcity.backend.payload;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class DailySummary {
    private LocalDateTime date;
    
    // Sales Overview
    private BigDecimal totalRevenue;
    private BigDecimal netRevenue;
    private BigDecimal totalTax;
    private BigDecimal totalDiscounts;
    private Integer totalOrders;
    private BigDecimal averageOrderValue;
    
    // Order Statistics
    private Integer completedOrders;
    private Integer pendingOrders;
    private Integer cancelledOrders;
    private Integer refundedOrders;
    
    // Payment Methods
    private Map<String, PaymentSummary> paymentMethodSummary;
    
    // Product Performance
    private List<ProductSummary> topSellingProducts;
    private List<ProductSummary> lowStockProducts;
    
    // Customer Insights
    private Integer totalCustomers;
    private Integer newCustomers;
    private Integer repeatCustomers;
    
    // Hourly Analysis
    private Map<Integer, HourlySummary> hourlyBreakdown;
    
    // Staff Performance
    private List<CashierSummary> cashierPerformance;
    
    // Inventory Updates
    private Integer stockUpdates;
    private Integer lowStockAlerts;
    private List<String> outOfStockItems;
    
    @Data
    @Builder
    public static class PaymentSummary {
        private Integer transactionCount;
        private BigDecimal totalAmount;
        private Double percentageOfTotal;
    }
    
    @Data
    @Builder
    public static class ProductSummary {
        private String productId;
        private String productName;
        private Integer quantitySold;
        private BigDecimal revenue;
        private Integer currentStock;
        private Boolean needsRestock;
    }
    
    @Data
    @Builder
    public static class HourlySummary {
        private Integer hour;
        private Integer orderCount;
        private BigDecimal revenue;
        private Integer customerCount;
        private Double averageOrderValue;
    }
    
    @Data
    @Builder
    public static class CashierSummary {
        private String cashierId;
        private String cashierName;
        private Integer ordersProcessed;
        private BigDecimal totalSales;
        private Double averageOrderProcessingTime;
        private Integer customerServed;
    }
    
    // Additional Metrics
    private Map<String, Integer> ordersByCategory;
    private Map<String, BigDecimal> discountsByType;
    private BigDecimal averageItemsPerOrder;
    private Integer totalItemsSold;
    private Double conversionRate;
    private List<String> popularCategories;
    private Map<String, Integer> ordersByLocation;
    private List<String> activePromotions;
    private Map<String, BigDecimal> promotionPerformance;
    
    // Comparison with Previous Day
    private ComparisonMetrics previousDayComparison;
    
    @Data
    @Builder
    public static class ComparisonMetrics {
        private Double revenueGrowth;
        private Double orderGrowth;
        private Double customerGrowth;
        private Double averageOrderValueGrowth;
    }
}