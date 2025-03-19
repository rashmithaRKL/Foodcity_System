package com.foodcity.backend.payload;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class OrderStatistics {
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
    private Integer refundedOrders;
    
    private Map<String, BigDecimal> revenueByPaymentMethod;
    private Map<String, Integer> ordersByStatus;
    private Map<String, BigDecimal> hourlyRevenue;
    
    private List<TopProduct> topProducts;
    private List<TopCustomer> topCustomers;
    
    private BigDecimal totalDiscounts;
    private BigDecimal totalTax;
    private Double conversionRate;
    private Double refundRate;
    
    private Map<String, Integer> ordersByDayOfWeek;
    private Map<String, BigDecimal> revenueByCategory;
    
    @Data
    @Builder
    public static class TopProduct {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal revenue;
        private Double percentageOfTotal;
    }
    
    @Data
    @Builder
    public static class TopCustomer {
        private String customerId;
        private String customerName;
        private Integer orderCount;
        private BigDecimal totalSpent;
        private Integer loyaltyPoints;
    }
    
    // Additional metrics
    private Map<String, Double> peakHours;
    private Map<String, Integer> ordersByLocation;
    private BigDecimal averageItemsPerOrder;
    private Map<String, Double> categoryDistribution;
    private Map<String, Integer> newVsRepeatCustomers;
    private BigDecimal averageProcessingTime;
    private Map<String, Integer> ordersByDeviceType;
    private Map<String, Double> promotionEffectiveness;
}