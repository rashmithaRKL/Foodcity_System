package com.foodcity.backend.controller;

import com.foodcity.backend.model.Payment;
import com.foodcity.backend.payload.ApiResponse;
import com.foodcity.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse> processPayment(@Valid @RequestBody Payment payment) {
        Payment processedPayment = paymentService.processPayment(payment);
        
        // Notify about new payment
        messagingTemplate.convertAndSend("/topic/payments/new", processedPayment);
        
        return ResponseEntity.ok(new ApiResponse(true, "Payment processed successfully", processedPayment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Page<Payment>> getAllPayments(
            @RequestParam(required = false) Payment.PaymentStatus status,
            @RequestParam(required = false) Payment.PaymentMethod method,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        Page<Payment> payments = paymentService.getAllPayments(status, method, startDate, endDate, pageable);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> refundPayment(
            @PathVariable String id,
            @RequestParam(required = false) String reason) {
        Payment refundedPayment = paymentService.refundPayment(id, reason);
        
        // Notify about refund
        messagingTemplate.convertAndSend("/topic/payments/" + id + "/refund", refundedPayment);
        
        return ResponseEntity.ok(new ApiResponse(true, "Payment refunded successfully", refundedPayment));
    }

    @GetMapping("/daily-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDailySummary(
            @RequestParam(required = false) LocalDateTime date) {
        Map<String, Object> summary = paymentService.getDailySummary(
                date != null ? date : LocalDateTime.now());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> statistics = paymentService.getPaymentStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/methods/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentMethodsSummary(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> summary = paymentService.getPaymentMethodsSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse> verifyPayment(@RequestBody Map<String, Object> paymentDetails) {
        boolean isValid = paymentService.verifyPayment(paymentDetails);
        return ResponseEntity.ok(new ApiResponse(true, "Payment verification completed", isValid));
    }

    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getFailedPayments(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        List<Payment> failedPayments = paymentService.getFailedPayments(startDate, endDate);
        return ResponseEntity.ok(failedPayments);
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse> retryPayment(@PathVariable String id) {
        Payment retriedPayment = paymentService.retryPayment(id);
        return ResponseEntity.ok(new ApiResponse(true, "Payment retry processed", retriedPayment));
    }

    @GetMapping("/reconciliation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReconciliationReport(
            @RequestParam LocalDateTime date) {
        Map<String, Object> report = paymentService.getReconciliationReport(date);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<Payment>> getCustomerPayments(@PathVariable String customerId) {
        List<Payment> payments = paymentService.getCustomerPayments(customerId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<Payment>> searchPayments(
            @RequestParam String query,
            @RequestParam(required = false) Payment.PaymentMethod method) {
        List<Payment> payments = paymentService.searchPayments(query, method);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> processBatchPayments(
            @RequestBody List<Payment> payments) {
        List<Payment> processedPayments = paymentService.processBatchPayments(payments);
        return ResponseEntity.ok(new ApiResponse(true, "Batch payments processed", processedPayments));
    }

    @GetMapping("/gateway-health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentGatewayHealth() {
        Map<String, Object> health = paymentService.getPaymentGatewayHealth();
        return ResponseEntity.ok(health);
    }
}