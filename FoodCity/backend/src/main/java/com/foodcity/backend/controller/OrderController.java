package com.foodcity.backend.controller;

import com.foodcity.backend.model.Order;
import com.foodcity.backend.payload.ApiResponse;
import com.foodcity.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody Order order) {
        Order createdOrder = orderService.createOrder(order);
        
        // Notify relevant parties about new order
        messagingTemplate.convertAndSend("/topic/orders/new", createdOrder);
        
        return ResponseEntity.ok(new ApiResponse(true, "Order created successfully", createdOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        Page<Order> orders = orderService.getAllOrders(status, startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable String id,
            @RequestParam Order.OrderStatus status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        
        // Notify about order status change
        messagingTemplate.convertAndSend("/topic/orders/" + id + "/status", updatedOrder);
        
        return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", updatedOrder));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> refundOrder(
            @PathVariable String id,
            @RequestParam(required = false) String reason) {
        Order refundedOrder = orderService.refundOrder(id, reason);
        
        // Notify about order refund
        messagingTemplate.convertAndSend("/topic/orders/" + id + "/refund", refundedOrder);
        
        return ResponseEntity.ok(new ApiResponse(true, "Order refunded successfully", refundedOrder));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable String customerId) {
        List<Order> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/cashier/{cashierId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getCashierOrders(
            @PathVariable String cashierId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        List<Order> orders = orderService.getCashierOrders(cashierId, startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/daily-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getDailySummary(
            @RequestParam(required = false) LocalDateTime date) {
        var summary = orderService.getDailySummary(date != null ? date : LocalDateTime.now());
        return ResponseEntity.ok(new ApiResponse(true, "Daily summary retrieved successfully", summary));
    }

    @GetMapping("/invoice/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<byte[]> generateInvoice(@PathVariable String orderId) {
        byte[] invoice = orderService.generateInvoice(orderId);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=invoice-" + orderId + ".pdf")
                .body(invoice);
    }

    @PostMapping("/bulk-status-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> bulkUpdateOrderStatus(
            @RequestBody List<String> orderIds,
            @RequestParam Order.OrderStatus status) {
        List<Order> updatedOrders = orderService.bulkUpdateOrderStatus(orderIds, status);
        
        // Notify about bulk status update
        messagingTemplate.convertAndSend("/topic/orders/bulk-update", updatedOrders);
        
        return ResponseEntity.ok(new ApiResponse(true, "Orders updated successfully", updatedOrders));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<Order>> searchOrders(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) Order.PaymentMethod paymentMethod) {
        List<Order> orders = orderService.searchOrders(customerName, invoiceNumber, paymentMethod);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getOrderStatistics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        var statistics = orderService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse(true, "Statistics retrieved successfully", statistics));
    }
}