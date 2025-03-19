package com.foodcity.backend.controller;

import com.foodcity.backend.model.Employee;
import com.foodcity.backend.payload.ApiResponse;
import com.foodcity.backend.service.EmployeeService;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<ApiResponse> createEmployee(@Valid @RequestBody Employee employee) {
        Employee savedEmployee = employeeService.createEmployee(employee);
        
        // Notify about new employee
        messagingTemplate.convertAndSend("/topic/employees/new", savedEmployee);
        
        return ResponseEntity.ok(new ApiResponse(true, "Employee created successfully", savedEmployee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Employee.Department department,
            @RequestParam(required = false) Employee.Position position,
            Pageable pageable) {
        Page<Employee> employees = employeeService.getAllEmployees(search, department, position, pageable);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody Employee employee) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employee);
        
        // Notify about employee update
        messagingTemplate.convertAndSend("/topic/employees/" + id, updatedEmployee);
        
        return ResponseEntity.ok(new ApiResponse(true, "Employee updated successfully", updatedEmployee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
        
        // Notify about employee deletion
        messagingTemplate.convertAndSend("/topic/employees/deleted", id);
        
        return ResponseEntity.ok(new ApiResponse(true, "Employee deleted successfully"));
    }

    @PostMapping("/{id}/attendance")
    public ResponseEntity<ApiResponse> recordAttendance(
            @PathVariable String id,
            @RequestParam Employee.AttendanceStatus status,
            @RequestParam(required = false) String notes) {
        Employee employee = employeeService.recordAttendance(id, status, notes);
        return ResponseEntity.ok(new ApiResponse(true, "Attendance recorded successfully", employee));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<ApiResponse> applyLeave(
            @PathVariable String id,
            @RequestBody Employee.Leave leaveRequest) {
        Employee employee = employeeService.applyLeave(id, leaveRequest);
        return ResponseEntity.ok(new ApiResponse(true, "Leave application submitted successfully", employee));
    }

    @PutMapping("/{id}/leave/{leaveId}")
    public ResponseEntity<ApiResponse> updateLeaveStatus(
            @PathVariable String id,
            @PathVariable String leaveId,
            @RequestParam Employee.LeaveStatus status) {
        Employee employee = employeeService.updateLeaveStatus(id, leaveId, status);
        return ResponseEntity.ok(new ApiResponse(true, "Leave status updated successfully", employee));
    }

    @PostMapping("/{id}/performance")
    public ResponseEntity<ApiResponse> addPerformanceReview(
            @PathVariable String id,
            @RequestBody Employee.Performance review) {
        Employee employee = employeeService.addPerformanceReview(id, review);
        return ResponseEntity.ok(new ApiResponse(true, "Performance review added successfully", employee));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(
            @PathVariable Employee.Department department) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/attendance")
    public ResponseEntity<Map<String, Object>> getAttendanceReport(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> report = employeeService.getAttendanceReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/leave-report")
    public ResponseEntity<Map<String, Object>> getLeaveReport(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> report = employeeService.getLeaveReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/performance-report")
    public ResponseEntity<Map<String, Object>> getPerformanceReport(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> report = employeeService.getPerformanceReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<ApiResponse> bulkUpdateEmployees(
            @RequestBody List<Employee> employees) {
        List<Employee> updatedEmployees = employeeService.bulkUpdateEmployees(employees);
        return ResponseEntity.ok(new ApiResponse(true, "Employees updated successfully", updatedEmployees));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(
            @RequestParam String query,
            @RequestParam(required = false) Employee.Department department) {
        List<Employee> employees = employeeService.searchEmployees(query, department);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}/salary-history")
    public ResponseEntity<List<Map<String, Object>>> getSalaryHistory(@PathVariable String id) {
        List<Map<String, Object>> salaryHistory = employeeService.getSalaryHistory(id);
        return ResponseEntity.ok(salaryHistory);
    }

    @PostMapping("/{id}/salary")
    public ResponseEntity<ApiResponse> updateSalary(
            @PathVariable String id,
            @RequestParam Double amount,
            @RequestParam(required = false) String reason) {
        Employee employee = employeeService.updateSalary(id, amount, reason);
        return ResponseEntity.ok(new ApiResponse(true, "Salary updated successfully", employee));
    }
}