package com.foodcity.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItem> items;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discountAmount;
    private String discountCode;
    
    private String customerId;
    private String customerName;
    private String cashierId;
    private String cashierName;
    
    private LocalDateTime orderDate;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String paymentId;
    private String invoiceNumber;
    
    private String notes;
    private boolean isRefunded;
    private LocalDateTime refundDate;
    private String refundReason;

    @Data
    public static class OrderItem {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private BigDecimal discountPerUnit;
        private String notes;
    }

    public enum OrderStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        CANCELLED,
        REFUNDED
    }

    public enum PaymentMethod {
        CASH,
        CREDIT_CARD,
        DEBIT_CARD,
        MOBILE_PAYMENT,
        ONLINE_BANKING
    }

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}