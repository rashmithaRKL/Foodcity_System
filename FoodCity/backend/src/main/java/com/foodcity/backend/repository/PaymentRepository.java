package com.foodcity.backend.repository;

import com.foodcity.backend.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    // Basic queries provided by MongoRepository
    
    // Find by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    boolean existsByTransactionId(String transactionId);

    // Find by status
    List<Payment> findByStatus(Payment.PaymentStatus status);
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    // Find by payment method
    List<Payment> findByPaymentMethod(Payment.PaymentMethod method);
    Page<Payment> findByPaymentMethod(Payment.PaymentMethod method, Pageable pageable);

    // Find by date range
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by customer
    List<Payment> findByCustomerId(String customerId);
    Page<Payment> findByCustomerId(String customerId, Pageable pageable);

    // Find by order
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByOrderIdIn(List<String> orderIds);

    // Find failed payments
    List<Payment> findByStatusAndPaymentDateBetween(
            Payment.PaymentStatus status, 
            LocalDateTime startDate, 
            LocalDateTime endDate);

    // Find pending refunds
    List<Payment> findByRefundStatusAndRefundRequestDateBefore(
            Payment.RefundStatus status, 
            LocalDateTime date);

    // Custom queries

    // Find payments by status and method
    List<Payment> findByStatusAndPaymentMethod(
            Payment.PaymentStatus status, 
            Payment.PaymentMethod method);

    // Find high value transactions
    @Query("{ 'amount': { $gte: ?0 } }")
    List<Payment> findHighValueTransactions(double amount);

    // Find payments requiring reconciliation
    @Query("{ 'reconciliationStatus': 'PENDING', 'paymentDate': { $lt: ?0 } }")
    List<Payment> findPaymentsNeedingReconciliation(LocalDateTime thresholdDate);

    // Find disputed payments
    @Query("{ 'disputeStatus': { $exists: true, $ne: null } }")
    List<Payment> findDisputedPayments();

    // Find recurring payments
    @Query("{ 'recurringPaymentId': { $exists: true } }")
    List<Payment> findRecurringPayments();

    // Find payments by gateway response code
    List<Payment> findByGatewayResponseCode(String responseCode);

    // Count payments by method
    long countByPaymentMethod(Payment.PaymentMethod method);

    // Count payments by status
    long countByStatus(Payment.PaymentStatus status);

    // Find payments with specific metadata
    @Query("{ 'metadata.?0': ?1 }")
    List<Payment> findByMetadata(String key, String value);

    // Find payments requiring attention
    @Query("{ $or: [ " +
           "{ 'status': 'PENDING', 'paymentDate': { $lt: ?0 } }, " +
           "{ 'status': 'FAILED', 'retryCount': { $lt: ?1 } } " +
           "] }")
    List<Payment> findPaymentsRequiringAttention(LocalDateTime thresholdDate, int maxRetries);

    // Find payments by multiple statuses
    List<Payment> findByStatusIn(List<Payment.PaymentStatus> statuses);

    // Find payments by amount range
    List<Payment> findByAmountBetween(double minAmount, double maxAmount);

    // Find payments with refunds
    @Query("{ 'refundAmount': { $gt: 0 } }")
    List<Payment> findPaymentsWithRefunds();

    // Find payments by currency
    List<Payment> findByCurrencyCode(String currencyCode);

    // Find payments with specific flags
    List<Payment> findByFraudCheckFlag(boolean fraudCheckFlag);

    // Find payments by processing time
    @Query("{ 'processingTime': { $gt: ?0 } }")
    List<Payment> findByProcessingTimeGreaterThan(long processingTimeMs);

    // Delete old completed payments
    void deleteByStatusAndPaymentDateBefore(
            Payment.PaymentStatus status, 
            LocalDateTime date);

    // Find payments by batch ID
    List<Payment> findByBatchId(String batchId);

    // Find payments by gateway
    List<Payment> findByPaymentGateway(String gateway);

    // Find payments with specific error codes
    List<Payment> findByErrorCode(String errorCode);

    // Find payments requiring settlement
    @Query("{ 'settlementStatus': 'PENDING' }")
    List<Payment> findPaymentsRequiringSettlement();
}