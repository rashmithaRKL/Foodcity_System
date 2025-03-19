package com.foodcity.backend.service;

import com.foodcity.backend.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CustomerService {
    
    Customer createCustomer(Customer customer);
    
    Customer getCustomerById(String id);
    
    Page<Customer> getAllCustomers(String search, Customer.CustomerTier tier, Pageable pageable);
    
    Customer updateCustomer(String id, Customer customer);
    
    void deleteCustomer(String id);
    
    List<Customer> searchCustomers(String query, Customer.CustomerTier tier);
    
    Map<String, Object> getLoyaltyPointsInfo(String id);
    
    Customer addLoyaltyPoints(String id, Integer points, String reason);
    
    Map<String, Object> getPurchaseHistory(String id);
    
    Map<String, Object> getCustomerStatistics();
    
    List<Customer> getTopCustomers(int limit);
    
    List<Customer> bulkUpdateCustomers(List<Customer> customers);
    
    List<Customer> getInactiveCustomers(int days);
    
    Customer upgradeCustomerTier(String id, Customer.CustomerTier newTier);
    
    // Additional business methods
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    List<Customer> getCustomersByTier(Customer.CustomerTier tier);
    
    Map<String, Integer> getCustomerCountByTier();
    
    double calculateCustomerLifetimeValue(String id);
    
    List<Customer> getCustomersWithBirthdays(int daysAhead);
    
    Map<String, Object> getCustomerInsights(String id);
    
    void updateCustomerTierBasedOnPoints(String id);
    
    List<Map<String, Object>> getCustomerTrends();
    
    Map<String, Object> getCustomerEngagementMetrics(String id);
    
    List<Customer> getCustomersForMarketing();
    
    void processLoyaltyPointsExpiry();
    
    Map<String, Object> getLoyaltyProgramMetrics();
    
    List<Customer> getRecentlyActiveCustomers(int days);
    
    Map<String, Object> getCustomerFeedbackSummary(String id);
    
    void mergeCustomerAccounts(String sourceId, String targetId);
    
    List<Map<String, Object>> getCustomerSegments();
    
    Map<String, Object> predictCustomerChurnRisk(String id);
    
    void updateCustomerPreferences(String id, Map<String, Object> preferences);
    
    List<Map<String, Object>> getRecommendedProducts(String id);
}