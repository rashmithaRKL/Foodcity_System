package com.foodcity.backend.service.impl;

import com.foodcity.backend.exception.ResourceNotFoundException;
import com.foodcity.backend.model.Order;
import com.foodcity.backend.model.Product;
import com.foodcity.backend.payload.DailySummary;
import com.foodcity.backend.payload.OrderStatistics;
import com.foodcity.backend.repository.OrderRepository;
import com.foodcity.backend.repository.ProductRepository;
import com.foodcity.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public Order createOrder(Order order) {
        validateOrder(order);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        updateInventoryAfterOrder(order);
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    public Page<Order> getAllOrders(String status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (status != null && startDate != null && endDate != null) {
            return orderRepository.findByStatusAndOrderDateBetween(
                Order.OrderStatus.valueOf(status), startDate, endDate, pageable);
        } else if (status != null) {
            return orderRepository.findByStatus(Order.OrderStatus.valueOf(status), pageable);
        } else if (startDate != null && endDate != null) {
            return orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    @Override
    public Order updateOrderStatus(String id, Order.OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public Order refundOrder(String id, String reason) {
        Order order = getOrderById(id);
        order.setStatus(Order.OrderStatus.REFUNDED);
        order.setRefunded(true);
        order.setRefundDate(LocalDateTime.now());
        order.setRefundReason(reason);
        // Reverse inventory changes
        reverseInventoryChanges(order);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getCustomerOrders(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> getCashierOrders(String cashierId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCashierIdAndOrderDateBetween(cashierId, startDate, endDate);
    }

    @Override
    public DailySummary getDailySummary(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        List<Order> dailyOrders = orderRepository.findByOrderDateBetween(startOfDay, endOfDay);
        
        return DailySummary.builder()
                .date(date)
                .totalRevenue(calculateTotalRevenue(dailyOrders))
                .totalOrders(dailyOrders.size())
                .completedOrders((int) dailyOrders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED).count())
                .pendingOrders((int) dailyOrders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count())
                .cancelledOrders((int) dailyOrders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED).count())
                .refundedOrders((int) dailyOrders.stream()
                        .filter(Order::isRefunded).count())
                .averageOrderValue(calculateAverageOrderValue(dailyOrders))
                .build();
    }

    @Override
    public byte[] generateInvoice(String orderId) {
        Order order = getOrderById(orderId);
        // Implementation for generating PDF invoice
        // This would typically use a PDF generation library like iText or Apache PDFBox
        return new byte[0]; // Placeholder
    }

    @Override
    public List<Order> bulkUpdateOrderStatus(List<String> orderIds, Order.OrderStatus status) {
        List<Order> orders = orderRepository.findAllById(orderIds);
        orders.forEach(order -> order.setStatus(status));
        return orderRepository.saveAll(orders);
    }

    @Override
    public List<Order> searchOrders(String customerName, String invoiceNumber, Order.PaymentMethod paymentMethod) {
        if (invoiceNumber != null) {
            return orderRepository.findByInvoiceNumber(invoiceNumber)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }
        if (customerName != null) {
            return orderRepository.searchByCustomerName(customerName);
        }
        if (paymentMethod != null) {
            return orderRepository.findByPaymentMethod(paymentMethod);
        }
        return Collections.emptyList();
    }

    @Override
    public OrderStatistics getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        
        return OrderStatistics.builder()
                .totalRevenue(calculateTotalRevenue(orders))
                .totalOrders(orders.size())
                .averageOrderValue(calculateAverageOrderValue(orders))
                .completedOrders((int) orders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED).count())
                .build();
    }

    @Override
    public Map<String, Object> getOrderAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("totalRevenue", calculateTotalRevenue(orders));
        analytics.put("orderCount", orders.size());
        analytics.put("averageOrderValue", calculateAverageOrderValue(orders));
        
        return analytics;
    }

    @Override
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.PENDING);
    }

    @Override
    public List<Order> getOrdersByPaymentMethod(Order.PaymentMethod paymentMethod) {
        return orderRepository.findByPaymentMethod(paymentMethod);
    }

    @Override
    public double calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return calculateTotalRevenue(orders).doubleValue();
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getPaymentMethod().toString(),
                        Collectors.summingDouble(order -> order.getTotalAmount().doubleValue())
                ));
    }

    @Override
    public Map<String, Integer> getOrderCountByStatus() {
        Map<String, Integer> statusCount = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            statusCount.put(status.toString(), 
                    Math.toIntExact(orderRepository.countByStatus(status)));
        }
        return statusCount;
    }

    @Override
    public List<Order> getHighValueOrders(double threshold) {
        return orderRepository.findHighValueOrders(threshold);
    }

    @Override
    public Map<String, Double> getRevenueByTimeOfDay(LocalDateTime date) {
        List<Order> orders = orderRepository.findByOrderDateBetween(
                date.toLocalDate().atStartOfDay(),
                date.toLocalDate().plusDays(1).atStartOfDay()
        );
        
        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> String.format("%02d:00", order.getOrderDate().getHour()),
                        Collectors.summingDouble(order -> order.getTotalAmount().doubleValue())
                ));
    }

    @Override
    public List<Order> getOrdersWithDiscounts() {
        return orderRepository.findOrdersWithDiscounts();
    }

    @Override
    public double calculateAverageOrderValue(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return calculateAverageOrderValue(orders).doubleValue();
    }

    @Override
    public Map<String, Integer> getTopSellingProducts(int limit) {
        // Implementation for getting top selling products
        return new HashMap<>();
    }

    @Override
    public List<Order> getFailedOrders() {
        return orderRepository.findByPaymentStatus(Order.PaymentStatus.FAILED);
    }

    @Override
    public void sendOrderConfirmation(String orderId) {
        // Implementation for sending order confirmation
        // This would typically integrate with an email service
    }

    @Override
    public void updateInventoryAfterOrder(Order order) {
        order.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProductId()));
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        });
    }

    @Override
    public boolean validateOrder(Order order) {
        // Validate order items, quantities, and prices
        return true;
    }

    @Override
    public Map<String, Object> generateOrderReport(String orderId) {
        Order order = getOrderById(orderId);
        Map<String, Object> report = new HashMap<>();
        // Populate report with order details
        return report;
    }

    @Override
    public List<Order> getOrdersRequiringAttention() {
        return orderRepository.findOrdersRequiringAttention(
                LocalDateTime.now().minusHours(1));
    }

    private void reverseInventoryChanges(Order order) {
        order.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProductId()));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });
    }

    private BigDecimal calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAverageOrderValue(List<Order> orders) {
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = calculateTotalRevenue(orders);
        return total.divide(BigDecimal.valueOf(orders.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
}