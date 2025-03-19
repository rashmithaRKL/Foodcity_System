package com.foodcity.backend.controller;

import com.foodcity.backend.model.Customer;
import com.foodcity.backend.payload.ApiResponse;
import com.foodcity.backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse> createCustomer(@Valid @RequestBody Customer customer) {
        Customer savedCustomer = customerService.createCustomer(customer);
        
        // Notify about new customer
        messagingTemplate.convertAndSend("/topic/customers/new", savedCustomer);
        
        return ResponseEntity.ok(new ApiResponse(true, "Customer created successfully", savedCustomer));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Page<Customer>> getAllCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Customer.CustomerTier tier,
            Pageable pageable) {
        Page<Customer> customers = customerService.getAllCustomers(search, tier, pageable);
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        
        // Notify about customer update
        messagingTemplate.convertAndSend("/topic/customers/" + id, updatedCustomer);
        
        return ResponseEntity.ok(new ApiResponse(true, "Customer updated successfully", updatedCustomer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer(id);
        
        // Notify about customer deletion
        messagingTemplate.convertAndSend("/topic/customers/deleted", id);
        
        return ResponseEntity.ok(new ApiResponse(true, "Customer deleted successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<Customer>> searchCustomers(
            @RequestParam String query,
            @RequestParam(required = false) Customer.CustomerTier tier) {
        List<Customer> customers = customerService.searchCustomers(query, tier);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}/loyalty-points")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Map<String, Object>> getLoyaltyPoints(@PathVariable String id) {
        Map<String, Object> loyaltyInfo = customerService.getLoyaltyPointsInfo(id);
        return ResponseEntity.ok(loyaltyInfo);
    }

    @PostMapping("/{id}/loyalty-points")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse> addLoyaltyPoints(
            @PathVariable String id,
            @RequestParam Integer points,
            @RequestParam(required = false) String reason) {
        Customer customer = customerService.addLoyaltyPoints(id, points, reason);
        return ResponseEntity.ok(new ApiResponse(true, "Loyalty points added successfully", customer));
    }

    @GetMapping("/{id}/purchase-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Map<String, Object>> getPurchaseHistory(@PathVariable String id) {
        Map<String, Object> purchaseHistory = customerService.getPurchaseHistory(id);
        return ResponseEntity.ok(purchaseHistory);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCustomerStatistics() {
        Map<String, Object> statistics = customerService.getCustomerStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/top")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Customer>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        List<Customer> topCustomers = customerService.getTopCustomers(limit);
        return ResponseEntity.ok(topCustomers);
    }

    @PostMapping("/bulk-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> bulkUpdateCustomers(
            @RequestBody List<Customer> customers) {
        List<Customer> updatedCustomers = customerService.bulkUpdateCustomers(customers);
        return ResponseEntity.ok(new ApiResponse(true, "Customers updated successfully", updatedCustomers));
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Customer>> getInactiveCustomers(
            @RequestParam(defaultValue = "30") int days) {
        List<Customer> inactiveCustomers = customerService.getInactiveCustomers(days);
        return ResponseEntity.ok(inactiveCustomers);
    }

    @PostMapping("/{id}/tier-upgrade")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> upgradeCustomerTier(
            @PathVariable String id,
            @RequestParam Customer.CustomerTier newTier) {
        Customer customer = customerService.upgradeCustomerTier(id, newTier);
        return ResponseEntity.ok(new ApiResponse(true, "Customer tier upgraded successfully", customer));
    }
}