package com.foodcity.backend.service;

import com.foodcity.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductService {
    
    Page<Product> getAllProducts(String category, String search, Pageable pageable);
    
    Product getProductById(String id);
    
    Product createProduct(Product product);
    
    Product updateProduct(String id, Product product);
    
    void deleteProduct(String id);
    
    List<Product> getProductsByCategory(String category);
    
    List<Product> getLowStockProducts();
    
    Product updateStock(String id, Integer quantity);
    
    List<Product> searchProducts(String query, String category);
    
    Product updatePrice(String id, Double price);
    
    Product getProductByBarcode(String barcode);
    
    List<Product> getActiveProducts();
    
    Product toggleProductStatus(String id);
    
    boolean existsByBarcode(String barcode);
    
    List<Product> getProductsBySupplier(String supplierId);
    
    void updateBulkPrices(List<String> productIds, Double percentageChange);
    
    List<Product> getOutOfStockProducts();
    
    List<Product> getExpiringSoonProducts(int daysThreshold);
}