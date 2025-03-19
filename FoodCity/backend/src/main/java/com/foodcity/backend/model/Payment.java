package com.foodcity.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;

    private String orderId;
    private String customerId;
    private String cashierId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal tipAmount;
    private BigDecimal totalAmount;

    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String referenceNumber;

    // For Card Payments
    private CardDetails cardDetails;
    
    // For Online Payments
    private OnlinePaymentDetails onlinePaymentDetails;

    // For Split Payments
    private List<SplitPayment> splitPayments;

    private LocalDateTime paymentDate;
    private LocalDateTime processedDate;
    private String processingNotes;

    // Refund Information
    private RefundDetails refundDetails;
    
    // Receipt Information
    private String receiptNumber;
    private boolean receiptGenerated;
    private String receiptUrl;

    @Data
    public static class CardDetails {
        private String cardType;
        private String lastFourDigits;
        private String cardHolderName;
        private String authorizationCode;
        private String terminalId;
    }

    @Data
    public static class OnlinePaymentDetails {
        private String provider;
        private String accountId;
        private String paymentGatewayResponse;
        private String confirmationCode;
    }

    @Data
    public static class SplitPayment {
        private PaymentMethod method;
        private BigDecimal amount;
        private String transactionId;
        private PaymentStatus status;
        private LocalDateTime processedDate;
    }

    @Data
    public static class RefundDetails {
        private BigDecimal refundAmount;
        private String refundReason;
        private String refundedBy;
        private LocalDateTime refundDate;
        private String refundTransactionId;
        private RefundStatus status;
        private String notes;
    }

    public enum PaymentMethod {
        CASH,
        CREDIT_CARD,
        DEBIT_CARD,
        MOBILE_PAYMENT,
        ONLINE_BANKING,
        GIFT_CARD,
        LOYALTY_POINTS
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        DECLINED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        CANCELLED
    }

    public enum RefundStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public enum CardType {
        VISA,
        MASTERCARD,
        AMERICAN_EXPRESS,
        DISCOVER,
        OTHER
    }
}