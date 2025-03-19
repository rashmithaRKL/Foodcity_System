package com.foodcity.backend.service;

import com.foodcity.backend.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PaymentService {
    
    Payment processPayment(Payment payment);
    
    Payment getPaymentById(String id);
    
    Page<Payment> getAllPayments(Payment.PaymentStatus status, Payment.PaymentMethod method,
                               LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Payment refundPayment(String id, String reason);
    
    Map<String, Object> getDailySummary(LocalDateTime date);
    
    Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getPaymentMethodsSummary(LocalDateTime startDate, LocalDateTime endDate);
    
    boolean verifyPayment(Map<String, Object> paymentDetails);
    
    List<Payment> getFailedPayments(LocalDateTime startDate, LocalDateTime endDate);
    
    Payment retryPayment(String id);
    
    Map<String, Object> getReconciliationReport(LocalDateTime date);
    
    List<Payment> getCustomerPayments(String customerId);
    
    List<Payment> searchPayments(String query, Payment.PaymentMethod method);
    
    List<Payment> processBatchPayments(List<Payment> payments);
    
    Map<String, Object> getPaymentGatewayHealth();
    
    // Additional business methods
    
    void validatePayment(Payment payment);
    
    Map<String, Object> getTransactionMetrics();
    
    List<Payment> getPendingPayments();
    
    Map<String, Double> getRevenueByPaymentMethod();
    
    void processRefunds();
    
    Map<String, Object> getFraudDetectionReport();
    
    List<Payment> getHighValueTransactions(double threshold);
    
    Map<String, Object> getPaymentTrends();
    
    void updatePaymentStatus(String id, Payment.PaymentStatus status);
    
    Map<String, Object> getChargebackAnalytics();
    
    List<Payment> getRecurringPayments();
    
    void processSubscriptionPayments();
    
    Map<String, Object> getPaymentSuccessRates();
    
    List<Map<String, Object>> getPaymentAnomalies();
    
    void handlePaymentWebhook(Map<String, Object> webhookData);
    
    Map<String, Object> getPaymentGatewayLogs();
    
    List<Payment> getDeclinedTransactions();
    
    Map<String, Object> getSettlementReport(LocalDateTime date);
    
    void processPaymentDisputes(String paymentId, Map<String, Object> disputeDetails);
    
    Map<String, Object> getDisputeMetrics();
}