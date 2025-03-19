package com.foodcity.backend.repository;

import com.foodcity.backend.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

    // Basic queries provided by MongoRepository
    
    // Find by email and phone
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // Search customers
    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);
    
    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Find by tier
    Page<Customer> findByTier(Customer.CustomerTier tier, Pageable pageable);
    List<Customer> findByTier(Customer.CustomerTier tier);

    // Find active/inactive customers
    List<Customer> findByActiveTrue();
    List<Customer> findByActiveFalse();

    // Find by loyalty points range
    List<Customer> findByLoyaltyPointsGreaterThanEqual(Integer points);
    List<Customer> findByLoyaltyPointsBetween(Integer minPoints, Integer maxPoints);

    // Find customers by last purchase date
    List<Customer> findByLastPurchaseDateBefore(LocalDateTime date);
    List<Customer> findByLastPurchaseDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find top customers by total purchases
    List<Customer> findTop10ByOrderByTotalPurchasesDesc();
    
    // Custom queries

    // Search customers by multiple criteria
    @Query("{ $or: [ " +
           "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'lastName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'email': { $regex: ?0, $options: 'i' } }, " +
           "{ 'phone': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<Customer> searchCustomers(String searchTerm);

    // Find customers with upcoming birthdays
    @Query("{ 'dateOfBirth': { $exists: true } }")
    List<Customer> findCustomersWithBirthdays();

    // Find customers by tier and minimum points
    @Query("{ 'tier': ?0, 'loyaltyPoints': { $gte: ?1 } }")
    List<Customer> findByTierAndMinimumPoints(Customer.CustomerTier tier, Integer minPoints);

    // Find customers who haven't made a purchase in given days
    @Query("{ 'lastPurchaseDate': { $lt: ?0 } }")
    List<Customer> findInactiveCustomers(LocalDateTime thresholdDate);

    // Find customers eligible for tier upgrade
    @Query("{ 'loyaltyPoints': { $gte: ?0 }, 'tier': { $ne: ?1 } }")
    List<Customer> findCustomersEligibleForTierUpgrade(Integer pointsThreshold, Customer.CustomerTier nextTier);

    // Count customers by tier
    long countByTier(Customer.CustomerTier tier);

    // Find customers with expired loyalty points
    @Query("{ 'loyaltyTransactions.expiryDate': { $lt: ?0 } }")
    List<Customer> findCustomersWithExpiredPoints(LocalDateTime currentDate);

    // Find customers by registration date range
    List<Customer> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find customers with high value purchases
    @Query("{ 'totalPurchases': { $gt: ?0 } }")
    List<Customer> findHighValueCustomers(Double purchaseThreshold);

    // Find customers by preferred payment method
    List<Customer> findByPreferredPaymentMethod(String paymentMethod);

    // Find customers with recent activity
    @Query("{ $or: [ " +
           "{ 'lastPurchaseDate': { $gte: ?0 } }, " +
           "{ 'lastLoginDate': { $gte: ?0 } } " +
           "] }")
    List<Customer> findRecentlyActiveCustomers(LocalDateTime thresholdDate);

    // Find customers with incomplete profiles
    @Query("{ $or: [ " +
           "{ 'email': { $exists: false } }, " +
           "{ 'phone': { $exists: false } }, " +
           "{ 'address': { $exists: false } } " +
           "] }")
    List<Customer> findCustomersWithIncompleteProfiles();

    // Find customers by total orders count
    List<Customer> findByTotalOrdersGreaterThan(Integer orderCount);

    // Find customers by city or region
    List<Customer> findByCity(String city);
    List<Customer> findByState(String state);

    // Delete inactive customers
    void deleteByActiveIsFalseAndLastPurchaseDateBefore(LocalDateTime thresholdDate);
}