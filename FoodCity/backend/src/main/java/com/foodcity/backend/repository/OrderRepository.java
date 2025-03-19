package com.foodcity.backend.repository;

import com.foodcity.backend.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    // Basic queries provided by MongoRepository
    
    // Find orders by customer
    List<Order> findByCustomerId(String customerId);
    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    // Find orders by cashier
    List<Order> findByCashierId(String cashierId);
    List<Order> findByCashierIdAndOrderDateBetween(String cashierId, LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by status
    List<Order> findByStatus(Order.OrderStatus status);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    // Find orders by date range
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find orders by payment method
    List<Order> findByPaymentMethod(Order.PaymentMethod paymentMethod);

    // Find orders by invoice number
    Optional<Order> findByInvoiceNumber(String invoiceNumber);

    // Find orders by status and date range
    List<Order> findByStatusAndOrderDateBetween(
        Order.OrderStatus status, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );

    // Custom queries for analytics
    
    // Count orders by status
    long countByStatus(Order.OrderStatus status);

    // Count orders by payment method
    long countByPaymentMethod(Order.PaymentMethod paymentMethod);

    // Find high value orders
    @Query("{ 'totalAmount': { $gte: ?0 } }")
    List<Order> findHighValueOrders(double amount);

    // Find orders with specific items
    @Query("{ 'items.productId': ?0 }")
    List<Order> findOrdersContainingProduct(String productId);

    // Find orders with discounts
    @Query("{ 'discountAmount': { $gt: 0 } }")
    List<Order> findOrdersWithDiscounts();

    // Find failed payments
    List<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus);

    // Find refunded orders
    @Query("{ 'isRefunded': true }")
    List<Order> findRefundedOrders();

    // Search orders by customer name
    @Query("{ $or: [ { 'customerName': { $regex: ?0, $options: 'i' } } ] }")
    List<Order> searchByCustomerName(String customerName);

    // Find orders requiring attention (pending for too long)
    @Query("{ 'status': 'PENDING', 'orderDate': { $lt: ?0 } }")
    List<Order> findOrdersRequiringAttention(LocalDateTime threshold);

    // Analytics queries

    // Get daily order count
    @Query(value = "{ 'orderDate': { $gte: ?0, $lt: ?1 } }", count = true)
    long getDailyOrderCount(LocalDateTime startOfDay, LocalDateTime endOfDay);

    // Get orders by hour
    @Query("{ 'orderDate': { $gte: ?0, $lt: ?1 } }")
    List<Order> getOrdersByHour(LocalDateTime startHour, LocalDateTime endHour);

    // Get average order value by date range
    @Query(value = "{ 'orderDate': { $gte: ?0, $lt: ?1 } }", fields = "{ 'totalAmount': 1 }")
    List<Order> getAverageOrderValue(LocalDateTime startDate, LocalDateTime endDate);

    // Find recent orders for a customer
    List<Order> findTop5ByCustomerIdOrderByOrderDateDesc(String customerId);

    // Find pending orders older than specified time
    List<Order> findByStatusAndOrderDateBefore(Order.OrderStatus status, LocalDateTime date);

    // Get orders grouped by payment method for a date range
    @Query(value = "{ 'orderDate': { $gte: ?0, $lt: ?1 } }", fields = "{ 'paymentMethod': 1, 'totalAmount': 1 }")
    List<Order> getOrdersByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate);
}
