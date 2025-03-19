package com.foodcity.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "employees")
public class Employee {
    @Id
    private String id;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    private String phone;
    private String address;
    
    // Employment Details
    @NotNull(message = "Position is required")
    private Position position;
    private Department department;
    private EmploymentStatus status;
    private LocalDateTime joiningDate;
    private LocalDateTime terminationDate;
    private String reportingManagerId;

    // Salary & Benefits
    private BigDecimal baseSalary;
    private BigDecimal hourlyRate;
    private List<Allowance> allowances;
    private List<Deduction> deductions;
    private PayFrequency payFrequency;
    private String bankAccountNumber;
    private String bankName;

    // Attendance & Leave
    private List<Attendance> attendanceRecords;
    private List<Leave> leaveRecords;
    private Integer totalLeaveBalance;
    private Integer sickLeaveBalance;
    private Integer casualLeaveBalance;

    // Performance & Skills
    private List<Performance> performanceRecords;
    private List<String> skills;
    private List<String> certifications;

    // System Details
    private boolean active = true;
    private LocalDateTime lastUpdated;
    private String updatedBy;

    @Data
    public static class Attendance {
        private LocalDateTime checkIn;
        private LocalDateTime checkOut;
        private AttendanceStatus status;
        private String notes;
    }

    @Data
    public static class Leave {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LeaveType type;
        private LeaveStatus status;
        private String reason;
        private String approvedBy;
        private LocalDateTime approvalDate;
    }

    @Data
    public static class Performance {
        private LocalDateTime reviewDate;
        private String reviewerId;
        private Integer rating;
        private String comments;
        private List<String> achievements;
        private List<String> improvements;
    }

    @Data
    public static class Allowance {
        private String type;
        private BigDecimal amount;
        private String description;
    }

    @Data
    public static class Deduction {
        private String type;
        private BigDecimal amount;
        private String description;
    }

    public enum Position {
        STORE_MANAGER,
        ASSISTANT_MANAGER,
        CASHIER,
        INVENTORY_MANAGER,
        SALES_ASSOCIATE,
        STOCK_CLERK,
        SECURITY_GUARD
    }

    public enum Department {
        MANAGEMENT,
        SALES,
        INVENTORY,
        ACCOUNTS,
        SECURITY,
        MAINTENANCE
    }

    public enum EmploymentStatus {
        FULL_TIME,
        PART_TIME,
        CONTRACT,
        PROBATION,
        TERMINATED
    }

    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        LATE,
        HALF_DAY,
        ON_LEAVE
    }

    public enum LeaveType {
        ANNUAL,
        SICK,
        CASUAL,
        MATERNITY,
        PATERNITY,
        UNPAID
    }

    public enum LeaveStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    public enum PayFrequency {
        WEEKLY,
        BI_WEEKLY,
        MONTHLY
    }
}