package com.foodcity.backend.service.impl;

import com.foodcity.backend.exception.ResourceNotFoundException;
import com.foodcity.backend.model.Customer;
import com.foodcity.backend.model.Order;
import com.foodcity.backend.repository.CustomerRepository;
import com.foodcity.backend.repository.OrderRepository;
import com.foodcity.backend.service.CustomerService;
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
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Override
    public Customer createCustomer(Customer customer) {
        validateNewCustomer(customer);
        customer.setRegistrationDate(LocalDateTime.now());
        customer.setActive(true);
        customer.setLoyaltyPoints(0);
        customer.setTier(Customer.CustomerTier.BRONZE);
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Override
    public Page<Customer> getAllCustomers(String search, Customer.CustomerTier tier, Pageable pageable) {
        if (search != null && tier != null) {
            return customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    search, search, pageable);
        } else if (tier != null) {
            return customerRepository.findByTier(tier, pageable);
        } else if (search != null) {
            return customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    search, search, pageable);
        }
        return customerRepository.findAll(pageable);
    }

    @Override
    public Customer updateCustomer(String id, Customer customerDetails) {
        Customer customer = getCustomerById(id);
        updateCustomerFields(customer, customerDetails);
        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(String id) {
        Customer customer = getCustomerById(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Override
    public List<Customer> searchCustomers(String query, Customer.CustomerTier tier) {
        if (tier != null) {
            return customerRepository.findByTierAndMinimumPoints(tier, 0);
        }
        return customerRepository.searchCustomers(query);
    }

    @Override
    public Map<String, Object> getLoyaltyPointsInfo(String id) {
        Customer customer = getCustomerById(id);
        Map<String, Object> loyaltyInfo = new HashMap<>();
        loyaltyInfo.put("currentPoints", customer.getLoyaltyPoints());
        loyaltyInfo.put("tier", customer.getTier());
        loyaltyInfo.put("nextTier", getNextTier(customer.getTier()));
        loyaltyInfo.put("pointsToNextTier", getPointsToNextTier(customer));
        loyaltyInfo.put("transactions", customer.getLoyaltyTransactions());
        return loyaltyInfo;
    }

    @Override
    public Customer addLoyaltyPoints(String id, Integer points, String reason) {
        Customer customer = getCustomerById(id);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        
        Customer.LoyaltyTransaction transaction = new Customer.LoyaltyTransaction();
        transaction.setPoints(points);
        transaction.setType(Customer.TransactionType.EARNED);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription(reason);
        
        customer.getLoyaltyTransactions().add(transaction);
        
        updateCustomerTierBasedOnPoints(customer);
        return customerRepository.save(customer);
    }

    @Override
    public Map<String, Object> getPurchaseHistory(String id) {
        List<Order> orders = orderRepository.findByCustomerId(id);
        Map<String, Object> history = new HashMap<>();
        history.put("totalOrders", orders.size());
        history.put("totalSpent", calculateTotalSpent(orders));
        history.put("averageOrderValue", calculateAverageOrderValue(orders));
        history.put("recentOrders", getRecentOrders(orders));
        return history;
    }

    @Override
    public Map<String, Object> getCustomerStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCustomers", customerRepository.count());
        statistics.put("activeCustomers", customerRepository.findByActiveTrue().size());
        statistics.put("customersByTier", getCustomerCountByTier());
        statistics.put("averageLoyaltyPoints", calculateAverageLoyaltyPoints());
        return statistics;
    }

    @Override
    public List<Customer> getTopCustomers(int limit) {
        return customerRepository.findTop10ByOrderByTotalPurchasesDesc()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> bulkUpdateCustomers(List<Customer> customers) {
        return customerRepository.saveAll(customers);
    }

    @Override
    public List<Customer> getInactiveCustomers(int days) {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(days);
        return customerRepository.findInactiveCustomers(thresholdDate);
    }

    @Override
    public Customer upgradeCustomerTier(String id, Customer.CustomerTier newTier) {
        Customer customer = getCustomerById(id);
        customer.setTier(newTier);
        return customerRepository.save(customer);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return customerRepository.existsByPhone(phone);
    }

    @Override
    public List<Customer> getCustomersByTier(Customer.CustomerTier tier) {
        return customerRepository.findByTier(tier);
    }

    @Override
    public Map<String, Integer> getCustomerCountByTier() {
        Map<String, Integer> countByTier = new HashMap<>();
        for (Customer.CustomerTier tier : Customer.CustomerTier.values()) {
            countByTier.put(tier.name(), Math.toIntExact(customerRepository.countByTier(tier)));
        }
        return countByTier;
    }

    @Override
    public double calculateCustomerLifetimeValue(String id) {
        List<Order> orders = orderRepository.findByCustomerId(id);
        return calculateTotalSpent(orders);
    }

    @Override
    public List<Customer> getCustomersWithBirthdays(int daysAhead) {
        return customerRepository.findCustomersWithBirthdays();
    }

    @Override
    public Map<String, Object> getCustomerInsights(String id) {
        Customer customer = getCustomerById(id);
        Map<String, Object> insights = new HashMap<>();
        insights.put("purchaseHistory", getPurchaseHistory(id));
        insights.put("loyaltyInfo", getLoyaltyPointsInfo(id));
        insights.put("lifetimeValue", calculateCustomerLifetimeValue(id));
        return insights;
    }

    @Override
    public void updateCustomerTierBasedOnPoints(String id) {
        Customer customer = getCustomerById(id);
        updateCustomerTierBasedOnPoints(customer);
        customerRepository.save(customer);
    }

    @Override
    public List<Map<String, Object>> getCustomerTrends() {
        // Implementation for analyzing customer trends
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getCustomerEngagementMetrics(String id) {
        // Implementation for calculating engagement metrics
        return new HashMap<>();
    }

    @Override
    public List<Customer> getCustomersForMarketing() {
        return customerRepository.findByActiveTrue();
    }

    @Override
    public void processLoyaltyPointsExpiry() {
        List<Customer> customers = customerRepository.findCustomersWithExpiredPoints(LocalDateTime.now());
        customers.forEach(this::processExpiredPoints);
        customerRepository.saveAll(customers);
    }

    @Override
    public Map<String, Object> getLoyaltyProgramMetrics() {
        // Implementation for loyalty program analytics
        return new HashMap<>();
    }

    @Override
    public List<Customer> getRecentlyActiveCustomers(int days) {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(days);
        return customerRepository.findRecentlyActiveCustomers(thresholdDate);
    }

    @Override
    public Map<String, Object> getCustomerFeedbackSummary(String id) {
        // Implementation for feedback summary
        return new HashMap<>();
    }

    @Override
    public void mergeCustomerAccounts(String sourceId, String targetId) {
        // Implementation for merging customer accounts
    }

    @Override
    public List<Map<String, Object>> getCustomerSegments() {
        // Implementation for customer segmentation
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> predictCustomerChurnRisk(String id) {
        // Implementation for churn risk prediction
        return new HashMap<>();
    }

    @Override
    public void updateCustomerPreferences(String id, Map<String, Object> preferences) {
        Customer customer = getCustomerById(id);
        // Implementation for updating preferences
        customerRepository.save(customer);
    }

    @Override
    public List<Map<String, Object>> getRecommendedProducts(String id) {
        // Implementation for product recommendations
        return new ArrayList<>();
    }

    // Private helper methods

    private void validateNewCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (customerRepository.existsByPhone(customer.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered");
        }
    }

    private void updateCustomerFields(Customer customer, Customer customerDetails) {
        customer.setFirstName(customerDetails.getFirstName());
        customer.setLastName(customerDetails.getLastName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        customer.setCity(customerDetails.getCity());
        customer.setState(customerDetails.getState());
        customer.setZipCode(customerDetails.getZipCode());
    }

    private Customer.CustomerTier getNextTier(Customer.CustomerTier currentTier) {
        switch (currentTier) {
            case BRONZE: return Customer.CustomerTier.SILVER;
            case SILVER: return Customer.CustomerTier.GOLD;
            case GOLD: return Customer.CustomerTier.PLATINUM;
            default: return currentTier;
        }
    }

    private int getPointsToNextTier(Customer customer) {
        Customer.CustomerTier nextTier = getNextTier(customer.getTier());
        return Math.max(0, nextTier.getRequiredPoints() - customer.getLoyaltyPoints());
    }

    private void updateCustomerTierBasedOnPoints(Customer customer) {
        int points = customer.getLoyaltyPoints();
        if (points >= Customer.CustomerTier.PLATINUM.getRequiredPoints()) {
            customer.setTier(Customer.CustomerTier.PLATINUM);
        } else if (points >= Customer.CustomerTier.GOLD.getRequiredPoints()) {
            customer.setTier(Customer.CustomerTier.GOLD);
        } else if (points >= Customer.CustomerTier.SILVER.getRequiredPoints()) {
            customer.setTier(Customer.CustomerTier.SILVER);
        }
    }

    private double calculateTotalSpent(List<Order> orders) {
        return orders.stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }

    private double calculateAverageOrderValue(List<Order> orders) {
        if (orders.isEmpty()) return 0.0;
        return calculateTotalSpent(orders) / orders.size();
    }

    private List<Order> getRecentOrders(List<Order> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private double calculateAverageLoyaltyPoints() {
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) return 0.0;
        return customers.stream()
                .mapToInt(Customer::getLoyaltyPoints)
                .average()
                .orElse(0.0);
    }

    private void processExpiredPoints(Customer customer) {
        // Implementation for processing expired loyalty points
    }
}