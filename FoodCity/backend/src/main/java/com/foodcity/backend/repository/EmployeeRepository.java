package com.foodcity.backend.repository;

import com.foodcity.backend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {

    // Basic queries provided by MongoRepository
    
    Optional<Employee> findByEmployeeId(String employeeId);
    Optional<Employee> findByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByEmail(String email);

    // Department related queries
    List<Employee> findByDepartment(Employee.Department department);
    Page<Employee> findByDepartment(Employee.Department department, Pageable pageable);
    long countByDepartment(Employee.Department department);

    // Position related queries
    List<Employee> findByPosition(Employee.Position position);
    Page<Employee> findByPosition(Employee.Position position, Pageable pageable);

    // Status related queries
    List<Employee> findByStatus(Employee.EmploymentStatus status);
    List<Employee> findByStatusAndDepartment(Employee.EmploymentStatus status, Employee.Department department);

    // Search queries
    Page<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);
    
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Manager related queries
    List<Employee> findByReportingManagerId(String managerId);
    long countByReportingManagerId(String managerId);

    // Leave related queries
    @Query("{ 'leaveRecords': { $elemMatch: { 'status': 'APPROVED', 'startDate': { $lte: ?0 }, 'endDate': { $gte: ?0 } } } }")
    List<Employee> findEmployeesOnLeave(LocalDateTime date);

    // Attendance related queries
    @Query("{ 'attendanceRecords': { $elemMatch: { 'date': { $gte: ?0, $lte: ?1 } } } }")
    List<Employee> findEmployeesWithAttendanceBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Performance related queries
    @Query("{ 'performanceRecords': { $elemMatch: { 'reviewDate': { $gte: ?0, $lte: ?1 } } } }")
    List<Employee> findEmployeesWithPerformanceReviewBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Salary related queries
    List<Employee> findByBaseSalaryLessThan(Double amount);
    List<Employee> findByBaseSalaryGreaterThan(Double amount);
    List<Employee> findByBaseSalaryBetween(Double minAmount, Double maxAmount);

    // Skills related queries
    List<Employee> findBySkillsContaining(String skill);
    @Query("{ 'skills': { $all: ?0 } }")
    List<Employee> findBySkillsAll(List<String> skills);

    // Training related queries
    @Query("{ 'certifications': { $exists: true, $ne: [] } }")
    List<Employee> findEmployeesWithCertifications();

    // Custom queries for specific business needs

    // Find employees whose contracts are expiring soon
    @Query("{ 'status': 'CONTRACT', 'terminationDate': { $lte: ?0 } }")
    List<Employee> findEmployeesWithExpiringContracts(LocalDateTime thresholdDate);

    // Find employees eligible for benefits
    @Query("{ 'joiningDate': { $lte: ?0 }, 'status': 'FULL_TIME' }")
    List<Employee> findEmployeesEligibleForBenefits(LocalDateTime thresholdDate);

    // Find employees needing performance review
    @Query("{ $or: [ " +
           "{ 'performanceRecords': { $size: 0 } }, " +
           "{ 'performanceRecords': { $not: { $elemMatch: { 'reviewDate': { $gte: ?0 } } } } } " +
           "] }")
    List<Employee> findEmployeesNeedingPerformanceReview(LocalDateTime thresholdDate);

    // Find employees by multiple departments
    List<Employee> findByDepartmentIn(List<Employee.Department> departments);

    // Find employees by attendance status for a specific date
    @Query("{ 'attendanceRecords': { $elemMatch: { 'date': ?0, 'status': ?1 } } }")
    List<Employee> findByAttendanceStatusAndDate(LocalDateTime date, Employee.AttendanceStatus status);

    // Find employees with pending leave requests
    @Query("{ 'leaveRecords': { $elemMatch: { 'status': 'PENDING' } } }")
    List<Employee> findEmployeesWithPendingLeaveRequests();

    // Find employees by pay frequency
    List<Employee> findByPayFrequency(Employee.PayFrequency payFrequency);

    // Find employees with high performance ratings
    @Query("{ 'performanceRecords': { $elemMatch: { 'rating': { $gte: ?0 } } } }")
    List<Employee> findHighPerformingEmployees(Integer minimumRating);

    // Find employees with specific allowances
    @Query("{ 'allowances.type': ?0 }")
    List<Employee> findEmployeesByAllowanceType(String allowanceType);

    // Find employees with deductions
    @Query("{ 'deductions': { $exists: true, $ne: [] } }")
    List<Employee> findEmployeesWithDeductions();

    // Delete terminated employees older than specified date
    void deleteByStatusAndTerminationDateBefore(
            Employee.EmploymentStatus status, LocalDateTime date);
}