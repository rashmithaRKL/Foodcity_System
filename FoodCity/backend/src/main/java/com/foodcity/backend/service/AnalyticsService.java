package com.foodcity.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    
    Map<String, Object> getDashboardMetrics(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getSalesAnalytics(LocalDateTime startDate, LocalDateTime endDate, String category);
    
    Map<String, Object> getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getCustomerAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getInventoryAnalytics(String category);
    
    Map<String, Object> getProductAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getEmployeeAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getTrendAnalysis(LocalDateTime startDate, LocalDateTime endDate, String metric);
    
    Map<String, Object> getForecasts(String metric, Integer periods);
    
    byte[] generateSalesReport(LocalDateTime startDate, LocalDateTime endDate, String format);
    
    byte[] generateInventoryReport(String category, String format);
    
    byte[] generateFinancialReport(LocalDateTime startDate, LocalDateTime endDate, String format);
    
    Map<String, Object> getKPIMetrics(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getPeriodComparisons(
            LocalDateTime period1Start, LocalDateTime period1End,
            LocalDateTime period2Start, LocalDateTime period2End,
            String metric);
    
    List<Map<String, Object>> getAnalyticsAlerts();
    
    List<Map<String, Object>> getBusinessRecommendations();
    
    // Additional analytics methods
    
    Map<String, Object> getSalesPerformanceMetrics(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getCustomerSegmentationAnalysis();
    
    Map<String, Object> getProductPerformanceMetrics(String productId);
    
    Map<String, Object> getInventoryOptimizationMetrics();
    
    Map<String, Object> getPricingAnalytics(String category);
    
    Map<String, Object> getMarketingCampaignEffectiveness(String campaignId);
    
    Map<String, Object> getCustomerRetentionMetrics();
    
    Map<String, Object> getEmployeePerformanceMetrics(String employeeId);
    
    Map<String, Object> getSupplierPerformanceAnalytics(String supplierId);
    
    Map<String, Object> getOperationalEfficiencyMetrics();
    
    Map<String, Object> getFinancialHealthMetrics();
    
    Map<String, Object> getProfitabilityAnalysis(String category);
    
    Map<String, Object> getCashFlowAnalytics();
    
    Map<String, Object> getSeasonalityAnalysis(String metric);
    
    Map<String, Object> getCompetitiveAnalysis();
    
    Map<String, Object> getMarketTrendAnalysis();
    
    Map<String, Object> getRiskAnalysis();
    
    Map<String, Object> getQualityMetrics();
    
    Map<String, Object> getCustomerFeedbackAnalytics();
    
    Map<String, Object> getWebsiteAnalytics();
    
    Map<String, Object> getMobileAppAnalytics();
    
    Map<String, Object> getSocialMediaMetrics();
    
    Map<String, Object> getEmailCampaignAnalytics();
    
    Map<String, Object> getLoyaltyProgramAnalytics();
    
    Map<String, Object> getPromotionEffectivenessAnalytics();
    
    Map<String, Object> getCrossSellingOpportunities();
    
    Map<String, Object> getUpsellAnalytics();
    
    Map<String, Object> getCustomerLifetimeValueAnalytics();
    
    Map<String, Object> getChurnRiskAnalytics();
    
    Map<String, Object> getPredictiveAnalytics(String metric);
    
    Map<String, Object> getAnomalyDetectionResults();
    
    Map<String, Object> getOptimizationRecommendations();
}