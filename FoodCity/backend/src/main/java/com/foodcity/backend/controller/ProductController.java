package com.foodcity.backend.controller;

import com.foodcity.backend.model.Product;
import com.foodcity.backend.payload.ApiResponse;
import com.foodcity.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<Product> products = productService.getAllProducts(category, search, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        
        // Notify subscribers about new product
        messagingTemplate.convertAndSend("/topic/products", savedProduct);
        
        return ResponseEntity.ok(new ApiResponse(true, "Product created successfully", savedProduct));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        
        // Notify subscribers about product update
        messagingTemplate.convertAndSend("/topic/products/" + id, updatedProduct);
        
        return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", updatedProduct));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        
        // Notify subscribers about product deletion
        messagingTemplate.convertAndSend("/topic/products/deleted", id);
        
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        List<Product> lowStockProducts = productService.getLowStockProducts();
        return ResponseEntity.ok(lowStockProducts);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse> updateStock(
            @PathVariable String id,
            @RequestParam Integer quantity) {
        Product updatedProduct = productService.updateStock(id, quantity);
        
        // Notify subscribers about stock update
        messagingTemplate.convertAndSend("/topic/products/" + id + "/stock", updatedProduct);
        
        return ResponseEntity.ok(new ApiResponse(true, "Stock updated successfully", updatedProduct));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam(required = false) String category) {
        List<Product> products = productService.searchProducts(query, category);
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}/price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updatePrice(
            @PathVariable String id,
            @RequestParam Double price) {
        Product updatedProduct = productService.updatePrice(id, price);
        
        // Notify subscribers about price update
        messagingTemplate.convertAndSend("/topic/products/" + id + "/price", updatedProduct);
        
        return ResponseEntity.ok(new ApiResponse(true, "Price updated successfully", updatedProduct));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Product> getProductByBarcode(@PathVariable String barcode) {
        Product product = productService.getProductByBarcode(barcode);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        List<Product> activeProducts = productService.getActiveProducts();
        return ResponseEntity.ok(activeProducts);
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> toggleProductStatus(@PathVariable String id) {
        Product updatedProduct = productService.toggleProductStatus(id);
        
        // Notify subscribers about status update
        messagingTemplate.convertAndSend("/topic/products/" + id + "/status", updatedProduct);
        
        return ResponseEntity.ok(new ApiResponse(true, "Product status updated successfully", updatedProduct));
    }
}