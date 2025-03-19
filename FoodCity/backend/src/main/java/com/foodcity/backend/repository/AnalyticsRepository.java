package com.foodcity.backend.repository;

import com.foodcity.backend.model.Analytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface AnalyticsRepository extends MongoRepository<Analytics, String> {

    // Sales Analytics Queries
    @Query("{ 'type': 'SALES', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findSalesAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'type': 'SALES', 'category': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<Analytics> findSalesAnalyticsByCategory(String category, LocalDateTime startDate, LocalDateTime endDate);

    // Revenue Analytics Queries
    @Query("{ 'type': 'REVENUE', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "{ 'type': 'REVENUE', 'timestamp': { $gte: ?0, $lte: ?1 } }", 
           fields = "{ 'amount': 1, 'category': 1 }")
    List<Map<String, Object>> findRevenueByCategory(LocalDateTime startDate, LocalDateTime endDate);

    // Customer Analytics Queries
    @Query("{ 'type': 'CUSTOMER', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findCustomerAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'type': 'CUSTOMER_SEGMENT' }")
    List<Analytics> findCustomerSegmentationData();

    // Inventory Analytics Queries
    @Query("{ 'type': 'INVENTORY', 'category': ?0 }")
    List<Analytics> findInventoryAnalyticsByCategory(String category);

    @Query("{ 'type': 'INVENTORY_MOVEMENT', 'timestamp': { $gte: ?0 } }")
    List<Analytics> findRecentInventoryMovements(LocalDateTime since);

    // Product Analytics Queries
    @Query("{ 'type': 'PRODUCT', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findProductAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'type': 'PRODUCT_PERFORMANCE', 'productId': ?0 }")
    List<Analytics> findProductPerformanceMetrics(String productId);

    // Employee Analytics Queries
    @Query("{ 'type': 'EMPLOYEE', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findEmployeeAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'type': 'EMPLOYEE_PERFORMANCE', 'employeeId': ?0 }")
    List<Analytics> findEmployeePerformanceMetrics(String employeeId);

    // Trend Analysis Queries
    @Query("{ 'type': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<Analytics> findTrendData(String metric, LocalDateTime startDate, LocalDateTime endDate);

    // KPI Metrics Queries
    @Query("{ 'type': 'KPI', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findKPIMetrics(LocalDateTime startDate, LocalDateTime endDate);

    // Marketing Analytics Queries
    @Query("{ 'type': 'CAMPAIGN', 'campaignId': ?0 }")
    List<Analytics> findMarketingCampaignMetrics(String campaignId);

    @Query("{ 'type': 'SOCIAL_MEDIA' }")
    List<Analytics> findSocialMediaMetrics();

    // Financial Analytics Queries
    @Query("{ 'type': 'FINANCIAL', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findFinancialMetrics(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'type': 'CASH_FLOW', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findCashFlowMetrics(LocalDateTime startDate, LocalDateTime endDate);

    // Customer Behavior Analytics
    @Query("{ 'type': 'CUSTOMER_BEHAVIOR', 'customerId': ?0 }")
    List<Analytics> findCustomerBehaviorMetrics(String customerId);

    @Query("{ 'type': 'CUSTOMER_LIFETIME_VALUE' }")
    List<Analytics> findCustomerLifetimeValueMetrics();

    // Operational Analytics
    @Query("{ 'type': 'OPERATIONAL_EFFICIENCY', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findOperationalEfficiencyMetrics(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'type': 'QUALITY_METRICS', 'timestamp': { $gte: ?0 } }")
    List<Analytics> findQualityMetrics(LocalDateTime since);

    // Risk Analytics
    @Query("{ 'type': 'RISK_ANALYSIS', 'category': ?0 }")
    List<Analytics> findRiskAnalytics(String category);

    @Query("{ 'type': 'ANOMALY_DETECTION', 'timestamp': { $gte: ?0 } }")
    List<Analytics> findAnomalyDetectionResults(LocalDateTime since);

    // Predictive Analytics
    @Query("{ 'type': 'PREDICTION', 'metric': ?0 }")
    List<Analytics> findPredictiveAnalytics(String metric);

    @Query("{ 'type': 'FORECAST', 'metric': ?0, 'periods': ?1 }")
    List<Analytics> findForecastData(String metric, Integer periods);

    // Supplier Analytics
    @Query("{ 'type': 'SUPPLIER_PERFORMANCE', 'supplierId': ?0 }")
    List<Analytics> findSupplierPerformanceMetrics(String supplierId);

    // Website and Mobile Analytics
    @Query("{ 'type': 'WEBSITE_ANALYTICS', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findWebsiteAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'type': 'MOBILE_APP_ANALYTICS', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findMobileAppAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    // Loyalty Program Analytics
    @Query("{ 'type': 'LOYALTY_PROGRAM', 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Analytics> findLoyaltyProgramMetrics(LocalDateTime startDate, LocalDateTime endDate);

    // Promotion Analytics
    @Query("{ 'type': 'PROMOTION_EFFECTIVENESS', 'promotionId': ?0 }")
    List<Analytics> findPromotionEffectivenessMetrics(String promotionId);

    // Cross-selling and Upselling Analytics
    @Query("{ 'type': 'CROSS_SELL_OPPORTUNITIES' }")
    List<Analytics> findCrossSellingOpportunities();

    @Query("{ 'type': 'UPSELL_ANALYTICS' }")
    List<Analytics> findUpsellAnalytics();

    // Delete old analytics data
    void deleteByTimestampBefore(LocalDateTime date);

    // Aggregate queries
    @Query(value = "{ 'type': ?0 }", count = true)
    long countAnalyticsByType(String type);

    // Custom aggregation queries can be implemented in the service layer
    // using MongoTemplate for more complex analytics requirements
}