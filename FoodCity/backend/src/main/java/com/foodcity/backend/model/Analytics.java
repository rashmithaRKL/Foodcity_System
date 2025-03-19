package com.foodcity.backend.model;

import lombok.Data;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
@Builder
@Document(collection = "analytics")
public class Analytics {
    
    @Id
    private String id;
    
    private String type;
    private LocalDateTime timestamp;
    private Map<String, Object> metrics;
    private Map<String, Object> dimensions;
    private Map<String, Object> metadata;

    // Analytics Types
    public enum AnalyticsType {
        SALES,
        REVENUE,
        CUSTOMER,
        INVENTORY,
        PRODUCT,
        EMPLOYEE,
        FINANCIAL,
        OPERATIONAL,
        MARKETING,
        SOCIAL_MEDIA,
        WEBSITE,
        MOBILE_APP
    }

    // Metric Categories
    @Data
    @Builder
    public static class SalesMetrics {
        private double totalSales;
        private double averageOrderValue;
        private int orderCount;
        private Map<String, Double> salesByCategory;
        private List<String> topSellingProducts;
        private double conversionRate;
    }

    @Data
    @Builder
    public static class RevenueMetrics {
        private double totalRevenue;
        private double netRevenue;
        private double grossMargin;
        private double operatingMargin;
        private Map<String, Double> revenueByChannel;
        private Map<String, Double> revenueByProduct;
    }

    @Data
    @Builder
    public static class CustomerMetrics {
        private int totalCustomers;
        private int newCustomers;
        private int activeCustomers;
        private double customerRetentionRate;
        private double customerLifetimeValue;
        private Map<String, Integer> customersBySegment;
    }

    @Data
    @Builder
    public static class InventoryMetrics {
        private int totalItems;
        private double totalValue;
        private double turnoverRate;
        private int lowStockItems;
        private int outOfStockItems;
        private Map<String, Integer> stockByCategory;
    }

    @Data
    @Builder
    public static class ProductMetrics {
        private int totalProducts;
        private List<String> bestPerformers;
        private List<String> poorPerformers;
        private Map<String, Double> profitMargins;
        private Map<String, Integer> productViews;
        private Map<String, Double> productRatings;
    }

    @Data
    @Builder
    public static class EmployeeMetrics {
        private int totalEmployees;
        private double averageProductivity;
        private double satisfactionRate;
        private double attendanceRate;
        private Map<String, Double> performanceScores;
        private Map<String, Integer> employeesByDepartment;
    }

    @Data
    @Builder
    public static class MarketingMetrics {
        private double campaignROI;
        private double conversionRate;
        private int leadGeneration;
        private Map<String, Double> channelEffectiveness;
        private Map<String, Integer> customerAcquisition;
        private List<String> topPerformingCampaigns;
    }

    @Data
    @Builder
    public static class OperationalMetrics {
        private double efficiency;
        private double processTime;
        private int errorRate;
        private Map<String, Double> resourceUtilization;
        private Map<String, Integer> incidentReports;
        private List<String> bottlenecks;
    }

    @Data
    @Builder
    public static class FinancialMetrics {
        private double cashFlow;
        private double profitability;
        private double operatingExpenses;
        private Map<String, Double> expensesByCategory;
        private Map<String, Double> revenueStreams;
        private List<String> costCenters;
    }

    @Data
    @Builder
    public static class WebsiteMetrics {
        private int visitors;
        private double bounceRate;
        private double averageSessionDuration;
        private Map<String, Integer> pageViews;
        private Map<String, Double> conversionsByPage;
        private List<String> topLandingPages;
    }

    @Data
    @Builder
    public static class MobileMetrics {
        private int activeUsers;
        private double retentionRate;
        private double crashRate;
        private Map<String, Integer> screenViews;
        private Map<String, Double> userEngagement;
        private List<String> popularFeatures;
    }

    @Data
    @Builder
    public static class SocialMediaMetrics {
        private int followers;
        private int engagement;
        private double sentimentScore;
        private Map<String, Integer> postReach;
        private Map<String, Double> platformPerformance;
        private List<String> trendingTopics;
    }

    // Dimension Types
    public enum DimensionType {
        TIME,
        LOCATION,
        PRODUCT,
        CUSTOMER,
        EMPLOYEE,
        CHANNEL,
        CAMPAIGN,
        DEVICE,
        PLATFORM
    }

    // Metadata Types
    public enum MetadataType {
        SOURCE,
        FREQUENCY,
        ACCURACY,
        COLLECTION_METHOD,
        PROCESSING_STATUS,
        VALIDATION_RULES
    }

    // Helper methods for analytics calculations
    public double calculateGrowth(double currentValue, double previousValue) {
        if (previousValue == 0) return 0;
        return ((currentValue - previousValue) / previousValue) * 100;
    }

    public double calculatePercentage(double value, double total) {
        if (total == 0) return 0;
        return (value / total) * 100;
    }

    public double calculateAverage(List<Double> values) {
        if (values == null || values.isEmpty()) return 0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) return 0;
        List<Double> sorted = values.stream().sorted().toList();
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
        }
        return sorted.get(middle);
    }

    public Map<String, Double> calculatePercentageDistribution(Map<String, Double> values) {
        double total = values.values().stream().mapToDouble(Double::doubleValue).sum();
        return values.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    e -> calculatePercentage(e.getValue(), total)
                ));
    }
}