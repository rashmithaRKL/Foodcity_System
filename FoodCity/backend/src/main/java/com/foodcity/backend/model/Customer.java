package com.foodcity.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "customers")
public class Customer {
    @Id
    private String id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;

    // Loyalty Program
    private Integer loyaltyPoints;
    private CustomerTier tier;
    private List<LoyaltyTransaction> loyaltyTransactions;
    
    // Purchase History
    private BigDecimal totalPurchases;
    private Integer totalOrders;
    private LocalDateTime lastPurchaseDate;
    private List<String> recentOrderIds;

    // Account Details
    private LocalDateTime registrationDate;
    private boolean active;
    private String preferredPaymentMethod;
    private String notes;
    private LocalDateTime lastUpdated;

    @Data
    public static class LoyaltyTransaction {
        private String orderId;
        private Integer points;
        private TransactionType type;
        private LocalDateTime date;
        private String description;
    }

    public enum CustomerTier {
        BRONZE(0),
        SILVER(1000),
        GOLD(5000),
        PLATINUM(10000);

        private final int requiredPoints;

        CustomerTier(int requiredPoints) {
            this.requiredPoints = requiredPoints;
        }

        public int getRequiredPoints() {
            return requiredPoints;
        }
    }

    public enum TransactionType {
        EARNED,
        REDEEMED,
        EXPIRED,
        ADJUSTED
    }
}