package com.foodcity.backend.controller;

import com.foodcity.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> metrics = analyticsService.getDashboardMetrics(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesAnalytics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(required = false) String category) {
        Map<String, Object> salesAnalytics = analyticsService.getSalesAnalytics(startDate, endDate, category);
        return ResponseEntity.ok(salesAnalytics);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> revenueAnalytics = analyticsService.getRevenueAnalytics(startDate, endDate);
        return ResponseEntity.ok(revenueAnalytics);
    }

    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> customerAnalytics = analyticsService.getCustomerAnalytics(startDate, endDate);
        return ResponseEntity.ok(customerAnalytics);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getInventoryAnalytics(
            @RequestParam(required = false) String category) {
        Map<String, Object> inventoryAnalytics = analyticsService.getInventoryAnalytics(category);
        return ResponseEntity.ok(inventoryAnalytics);
    }

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProductAnalytics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> productAnalytics = analyticsService.getProductAnalytics(startDate, endDate);
        return ResponseEntity.ok(productAnalytics);
    }

    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getEmployeeAnalytics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> employeeAnalytics = analyticsService.getEmployeeAnalytics(startDate, endDate);
        return ResponseEntity.ok(employeeAnalytics);
    }

    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrendAnalysis(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam String metric) {
        Map<String, Object> trends = analyticsService.getTrendAnalysis(startDate, endDate, metric);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/forecasts")
    public ResponseEntity<Map<String, Object>> getForecasts(
            @RequestParam String metric,
            @RequestParam Integer periods) {
        Map<String, Object> forecasts = analyticsService.getForecasts(metric, periods);
        return ResponseEntity.ok(forecasts);
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<byte[]> generateSalesReport(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam String format) {
        byte[] report = analyticsService.generateSalesReport(startDate, endDate, format);
        return ResponseEntity.ok()
                .header("Content-Type", getContentType(format))
                .header("Content-Disposition", "attachment; filename=sales-report." + format)
                .body(report);
    }

    @GetMapping("/reports/inventory")
    public ResponseEntity<byte[]> generateInventoryReport(
            @RequestParam String category,
            @RequestParam String format) {
        byte[] report = analyticsService.generateInventoryReport(category, format);
        return ResponseEntity.ok()
                .header("Content-Type", getContentType(format))
                .header("Content-Disposition", "attachment; filename=inventory-report." + format)
                .body(report);
    }

    @GetMapping("/reports/financial")
    public ResponseEntity<byte[]> generateFinancialReport(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam String format) {
        byte[] report = analyticsService.generateFinancialReport(startDate, endDate, format);
        return ResponseEntity.ok()
                .header("Content-Type", getContentType(format))
                .header("Content-Disposition", "attachment; filename=financial-report." + format)
                .body(report);
    }

    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Object>> getKPIMetrics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, Object> kpiMetrics = analyticsService.getKPIMetrics(startDate, endDate);
        return ResponseEntity.ok(kpiMetrics);
    }

    @GetMapping("/comparisons")
    public ResponseEntity<Map<String, Object>> getPeriodComparisons(
            @RequestParam LocalDateTime period1Start,
            @RequestParam LocalDateTime period1End,
            @RequestParam LocalDateTime period2Start,
            @RequestParam LocalDateTime period2End,
            @RequestParam String metric) {
        Map<String, Object> comparisons = analyticsService.getPeriodComparisons(
                period1Start, period1End, period2Start, period2End, metric);
        return ResponseEntity.ok(comparisons);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAnalyticsAlerts() {
        List<Map<String, Object>> alerts = analyticsService.getAnalyticsAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getBusinessRecommendations() {
        List<Map<String, Object>> recommendations = analyticsService.getBusinessRecommendations();
        return ResponseEntity.ok(recommendations);
    }

    // Helper method to determine content type based on format
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv":
                return "text/csv";
            default:
                return "application/octet-stream";
        }
    }
}