package com.foodcity.backend.service.impl;

import com.foodcity.backend.exception.ResourceNotFoundException;
import com.foodcity.backend.model.Employee;
import com.foodcity.backend.repository.EmployeeRepository;
import com.foodcity.backend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee createEmployee(Employee employee) {
        validateNewEmployee(employee);
        employee.setJoiningDate(LocalDateTime.now());
        employee.setStatus(Employee.EmploymentStatus.PROBATION);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee getEmployeeById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @Override
    public Page<Employee> getAllEmployees(String search, Employee.Department department,
                                       Employee.Position position, Pageable pageable) {
        if (search != null) {
            return employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    search, search, pageable);
        } else if (department != null) {
            return employeeRepository.findByDepartment(department, pageable);
        } else if (position != null) {
            return employeeRepository.findByPosition(position, pageable);
        }
        return employeeRepository.findAll(pageable);
    }

    @Override
    public Employee updateEmployee(String id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        updateEmployeeFields(employee, employeeDetails);
        return employeeRepository.save(employee);
    }

    @Override
    public void deleteEmployee(String id) {
        Employee employee = getEmployeeById(id);
        employee.setStatus(Employee.EmploymentStatus.TERMINATED);
        employee.setTerminationDate(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    @Override
    public Employee recordAttendance(String id, Employee.AttendanceStatus status, String notes) {
        Employee employee = getEmployeeById(id);
        
        Employee.Attendance attendance = new Employee.Attendance();
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setStatus(status);
        attendance.setNotes(notes);
        
        employee.getAttendanceRecords().add(attendance);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee applyLeave(String id, Employee.Leave leaveRequest) {
        Employee employee = getEmployeeById(id);
        leaveRequest.setStatus(Employee.LeaveStatus.PENDING);
        employee.getLeaveRecords().add(leaveRequest);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateLeaveStatus(String id, String leaveId, Employee.LeaveStatus status) {
        Employee employee = getEmployeeById(id);
        employee.getLeaveRecords().stream()
                .filter(leave -> leave.equals(leaveId))
                .findFirst()
                .ifPresent(leave -> {
                    leave.setStatus(status);
                    leave.setApprovalDate(LocalDateTime.now());
                });
        return employeeRepository.save(employee);
    }

    @Override
    public Employee addPerformanceReview(String id, Employee.Performance review) {
        Employee employee = getEmployeeById(id);
        review.setReviewDate(LocalDateTime.now());
        employee.getPerformanceRecords().add(review);
        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> getEmployeesByDepartment(Employee.Department department) {
        return employeeRepository.findByDepartment(department);
    }

    @Override
    public Map<String, Object> getAttendanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Employee> employees = employeeRepository.findEmployeesWithAttendanceBetween(startDate, endDate);
        Map<String, Object> report = new HashMap<>();
        
        report.put("totalEmployees", employees.size());
        report.put("presentCount", countAttendanceByStatus(employees, Employee.AttendanceStatus.PRESENT));
        report.put("absentCount", countAttendanceByStatus(employees, Employee.AttendanceStatus.ABSENT));
        report.put("lateCount", countAttendanceByStatus(employees, Employee.AttendanceStatus.LATE));
        
        return report;
    }

    @Override
    public Map<String, Object> getLeaveReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Employee> employees = employeeRepository.findAll();
        Map<String, Object> report = new HashMap<>();
        
        Map<Employee.LeaveType, Long> leavesByType = employees.stream()
                .flatMap(e -> e.getLeaveRecords().stream())
                .filter(leave -> isLeaveBetweenDates(leave, startDate, endDate))
                .collect(Collectors.groupingBy(Employee.Leave::getType, Collectors.counting()));
        
        report.put("leavesByType", leavesByType);
        report.put("totalLeaves", leavesByType.values().stream().mapToLong(Long::longValue).sum());
        
        return report;
    }

    @Override
    public Map<String, Object> getPerformanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Employee> employees = employeeRepository.findEmployeesWithPerformanceReviewBetween(startDate, endDate);
        Map<String, Object> report = new HashMap<>();
        
        double averageRating = employees.stream()
                .flatMap(e -> e.getPerformanceRecords().stream())
                .mapToInt(Employee.Performance::getRating)
                .average()
                .orElse(0.0);
        
        report.put("averageRating", averageRating);
        report.put("totalReviews", employees.size());
        
        return report;
    }

    @Override
    public List<Employee> bulkUpdateEmployees(List<Employee> employees) {
        return employeeRepository.saveAll(employees);
    }

    @Override
    public List<Employee> searchEmployees(String query, Employee.Department department) {
        if (department != null) {
            return employeeRepository.findByDepartment(department);
        }
        return employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
    }

    @Override
    public List<Map<String, Object>> getSalaryHistory(String id) {
        Employee employee = getEmployeeById(id);
        // Implementation for salary history tracking
        return new ArrayList<>();
    }

    @Override
    public Employee updateSalary(String id, Double amount, String reason) {
        Employee employee = getEmployeeById(id);
        employee.setBaseSalary(amount);
        // Add to salary history
        return employeeRepository.save(employee);
    }

    @Override
    public boolean existsByEmployeeId(String employeeId) {
        return employeeRepository.existsByEmployeeId(employeeId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Override
    public List<Employee> getEmployeesByManager(String managerId) {
        return employeeRepository.findByReportingManagerId(managerId);
    }

    @Override
    public Map<String, Object> getDepartmentMetrics(Employee.Department department) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("employeeCount", employeeRepository.countByDepartment(department));
        // Add more department metrics
        return metrics;
    }

    @Override
    public List<Employee> getEmployeesOnLeave() {
        return employeeRepository.findEmployeesOnLeave(LocalDateTime.now());
    }

    @Override
    public Map<String, Double> getDepartmentSalaryStats() {
        Map<String, Double> stats = new HashMap<>();
        // Implementation for salary statistics
        return stats;
    }

    @Override
    public List<Employee> getEmployeesForPayroll() {
        return employeeRepository.findByStatus(Employee.EmploymentStatus.FULL_TIME);
    }

    @Override
    public void processPayroll(LocalDateTime payPeriodStart, LocalDateTime payPeriodEnd) {
        // Implementation for payroll processing
    }

    @Override
    public Map<String, Object> getEmployeeWorkload(String id) {
        // Implementation for workload analysis
        return new HashMap<>();
    }

    @Override
    public List<Employee> getEmployeesNeedingTraining() {
        return employeeRepository.findEmployeesNeedingPerformanceReview(LocalDateTime.now().minusMonths(6));
    }

    @Override
    public void scheduleShift(String id, Map<String, Object> shiftDetails) {
        // Implementation for shift scheduling
    }

    @Override
    public Map<String, Object> getShiftSchedule(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for shift schedule
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getOvertimeReport(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for overtime reporting
        return new ArrayList<>();
    }

    @Override
    public void updateEmployeeSkills(String id, List<String> skills) {
        Employee employee = getEmployeeById(id);
        employee.setSkills(skills);
        employeeRepository.save(employee);
    }

    @Override
    public Map<String, Object> getSkillMatrix() {
        // Implementation for skill matrix
        return new HashMap<>();
    }

    @Override
    public List<Employee> getAvailableEmployees(LocalDateTime dateTime) {
        // Implementation for finding available employees
        return new ArrayList<>();
    }

    @Override
    public void assignTask(String employeeId, Map<String, Object> taskDetails) {
        // Implementation for task assignment
    }

    @Override
    public Map<String, Object> getTaskAssignments(String employeeId) {
        // Implementation for task assignments
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getProductivityMetrics(String employeeId) {
        // Implementation for productivity metrics
        return new ArrayList<>();
    }

    @Override
    public void recordTraining(String employeeId, Map<String, Object> trainingDetails) {
        // Implementation for training records
    }

    @Override
    public Map<String, Object> getTrainingHistory(String employeeId) {
        // Implementation for training history
        return new HashMap<>();
    }

    // Private helper methods

    private void validateNewEmployee(Employee employee) {
        if (employeeRepository.existsByEmployeeId(employee.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID already exists");
        }
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
    }

    private void updateEmployeeFields(Employee employee, Employee employeeDetails) {
        employee.setFirstName(employeeDetails.getFirstName());
        employee.setLastName(employeeDetails.getLastName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPhone(employeeDetails.getPhone());
        employee.setAddress(employeeDetails.getAddress());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setPosition(employeeDetails.getPosition());
        employee.setReportingManagerId(employeeDetails.getReportingManagerId());
    }

    private long countAttendanceByStatus(List<Employee> employees, Employee.AttendanceStatus status) {
        return employees.stream()
                .flatMap(e -> e.getAttendanceRecords().stream())
                .filter(a -> a.getStatus() == status)
                .count();
    }

    private boolean isLeaveBetweenDates(Employee.Leave leave, LocalDateTime startDate, LocalDateTime endDate) {
        return !leave.getStartDate().isBefore(startDate) && !leave.getEndDate().isAfter(endDate);
    }
}