package com.foodcity.backend.repository;

import com.foodcity.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Basic CRUD operations are provided by MongoRepository

    // Find by category with pagination
    Page<Product> findByCategory(String category, Pageable pageable);

    // Find by category without pagination
    List<Product> findByCategory(String category);

    // Search by name containing text (case-insensitive)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Product> findByNameContainingIgnoreCase(String name);

    // Search by category and name
    Page<Product> findByCategoryAndNameContainingIgnoreCase(String category, String name, Pageable pageable);
    List<Product> findByCategoryAndNameContainingIgnoreCase(String category, String name);

    // Find by barcode
    Optional<Product> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);

    // Find by supplier
    List<Product> findBySupplierId(String supplierId);

    // Find active products
    List<Product> findByActiveTrue();

    // Find products with low stock
    List<Product> findByStockQuantityLessThanEqual(Integer threshold);

    // Find out of stock products
    List<Product> findByStockQuantityEquals(Integer quantity);

    // Find products by price range
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find products with discounts
    @Query("{ 'discountPrice': { $exists: true, $ne: null } }")
    List<Product> findProductsWithDiscount();

    // Find products by multiple categories
    List<Product> findByCategoryIn(List<String> categories);

    // Find products that need reordering (stock below reorder point)
    @Query("{ 'stockQuantity': { $lte: '$reorderPoint' } }")
    List<Product> findProductsNeedingReorder();

    // Custom query to find products by name or description
    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<Product> searchByNameOrDescription(String searchTerm);

    // Find products updated after a certain date
    List<Product> findByLastUpdatedGreaterThan(String date);

    // Find products by status and category
    List<Product> findByActiveAndCategory(boolean active, String category);

    // Count products by category
    long countByCategory(String category);

    // Count active products
    long countByActiveTrue();

    // Count products with low stock
    long countByStockQuantityLessThanEqual(Integer threshold);

    // Delete products by category
    void deleteByCategory(String category);

    // Delete products by supplier
    void deleteBySupplierId(String supplierId);
}