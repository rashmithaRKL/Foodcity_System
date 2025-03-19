package com.foodcity.backend.service;

import com.foodcity.backend.model.Order;
import com.foodcity.backend.payload.OrderStatistics;
import com.foodcity.backend.payload.DailySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    
    Order createOrder(Order order);
    
    Order getOrderById(String id);
    
    Page<Order> getAllOrders(String status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Order updateOrderStatus(String id, Order.OrderStatus status);
    
    Order refundOrder(String id, String reason);
    
    List<Order> getCustomerOrders(String customerId);
    
    List<Order> getCashierOrders(String cashierId, LocalDateTime startDate, LocalDateTime endDate);
    
    DailySummary getDailySummary(LocalDateTime date);
    
    byte[] generateInvoice(String orderId);
    
    List<Order> bulkUpdateOrderStatus(List<String> orderIds, Order.OrderStatus status);
    
    List<Order> searchOrders(String customerName, String invoiceNumber, Order.PaymentMethod paymentMethod);
    
    OrderStatistics getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    // Additional business methods
    
    Map<String, Object> getOrderAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> getPendingOrders();
    
    List<Order> getOrdersByPaymentMethod(Order.PaymentMethod paymentMethod);
    
    double calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Double> getRevenueByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Integer> getOrderCountByStatus();
    
    List<Order> getHighValueOrders(double threshold);
    
    Map<String, Double> getRevenueByTimeOfDay(LocalDateTime date);
    
    List<Order> getOrdersWithDiscounts();
    
    double calculateAverageOrderValue(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Integer> getTopSellingProducts(int limit);
    
    List<Order> getFailedOrders();
    
    void sendOrderConfirmation(String orderId);
    
    void updateInventoryAfterOrder(Order order);
    
    boolean validateOrder(Order order);
    
    Map<String, Object> generateOrderReport(String orderId);
    
    List<Order> getOrdersRequiringAttention();
}