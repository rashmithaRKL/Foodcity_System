package com.foodcity.backend.service;

import com.foodcity.backend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EmployeeService {
    
    Employee createEmployee(Employee employee);
    
    Employee getEmployeeById(String id);
    
    Page<Employee> getAllEmployees(String search, Employee.Department department, 
                                 Employee.Position position, Pageable pageable);
    
    Employee updateEmployee(String id, Employee employee);
    
    void deleteEmployee(String id);
    
    Employee recordAttendance(String id, Employee.AttendanceStatus status, String notes);
    
    Employee applyLeave(String id, Employee.Leave leaveRequest);
    
    Employee updateLeaveStatus(String id, String leaveId, Employee.LeaveStatus status);
    
    Employee addPerformanceReview(String id, Employee.Performance review);
    
    List<Employee> getEmployeesByDepartment(Employee.Department department);
    
    Map<String, Object> getAttendanceReport(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getLeaveReport(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getPerformanceReport(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Employee> bulkUpdateEmployees(List<Employee> employees);
    
    List<Employee> searchEmployees(String query, Employee.Department department);
    
    List<Map<String, Object>> getSalaryHistory(String id);
    
    Employee updateSalary(String id, Double amount, String reason);
    
    // Additional business methods
    
    boolean existsByEmployeeId(String employeeId);
    
    boolean existsByEmail(String email);
    
    List<Employee> getEmployeesByManager(String managerId);
    
    Map<String, Object> getDepartmentMetrics(Employee.Department department);
    
    List<Employee> getEmployeesOnLeave();
    
    Map<String, Double> getDepartmentSalaryStats();
    
    List<Employee> getEmployeesForPayroll();
    
    void processPayroll(LocalDateTime payPeriodStart, LocalDateTime payPeriodEnd);
    
    Map<String, Object> getEmployeeWorkload(String id);
    
    List<Employee> getEmployeesNeedingTraining();
    
    void scheduleShift(String id, Map<String, Object> shiftDetails);
    
    Map<String, Object> getShiftSchedule(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Map<String, Object>> getOvertimeReport(LocalDateTime startDate, LocalDateTime endDate);
    
    void updateEmployeeSkills(String id, List<String> skills);
    
    Map<String, Object> getSkillMatrix();
    
    List<Employee> getAvailableEmployees(LocalDateTime dateTime);
    
    void assignTask(String employeeId, Map<String, Object> taskDetails);
    
    Map<String, Object> getTaskAssignments(String employeeId);
    
    List<Map<String, Object>> getProductivityMetrics(String employeeId);
    
    void recordTraining(String employeeId, Map<String, Object> trainingDetails);
    
    Map<String, Object> getTrainingHistory(String employeeId);
}