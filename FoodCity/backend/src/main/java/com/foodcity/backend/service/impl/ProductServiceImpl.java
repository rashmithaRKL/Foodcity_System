package com.foodcity.backend.service.impl;

import com.foodcity.backend.exception.ResourceNotFoundException;
import com.foodcity.backend.model.Product;
import com.foodcity.backend.repository.ProductRepository;
import com.foodcity.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> getAllProducts(String category, String search, Pageable pageable) {
        if (StringUtils.hasText(category) && StringUtils.hasText(search)) {
            return productRepository.findByCategoryAndNameContainingIgnoreCase(category, search, pageable);
        } else if (StringUtils.hasText(category)) {
            return productRepository.findByCategory(category, pageable);
        } else if (StringUtils.hasText(search)) {
            return productRepository.findByNameContainingIgnoreCase(search, pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    public Product createProduct(Product product) {
        if (product.getBarcode() != null && existsByBarcode(product.getBarcode())) {
            throw new IllegalArgumentException("Product with this barcode already exists");
        }
        product.setLastUpdated(LocalDateTime.now().toString());
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(String id, Product productDetails) {
        Product product = getProductById(id);
        
        if (productDetails.getBarcode() != null && 
            !productDetails.getBarcode().equals(product.getBarcode()) && 
            existsByBarcode(productDetails.getBarcode())) {
            throw new IllegalArgumentException("Product with this barcode already exists");
        }

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategory(productDetails.getCategory());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setImageUrl(productDetails.getImageUrl());
        product.setBarcode(productDetails.getBarcode());
        product.setSupplierId(productDetails.getSupplierId());
        product.setDiscountPrice(productDetails.getDiscountPrice());
        product.setLastUpdated(LocalDateTime.now().toString());

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(String id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThanEqual(10);
    }

    @Override
    public Product updateStock(String id, Integer quantity) {
        Product product = getProductById(id);
        product.setStockQuantity(quantity);
        product.setLastUpdated(LocalDateTime.now().toString());
        return productRepository.save(product);
    }

    @Override
    public List<Product> searchProducts(String query, String category) {
        if (StringUtils.hasText(category)) {
            return productRepository.findByCategoryAndNameContainingIgnoreCase(category, query);
        }
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    @Override
    public Product updatePrice(String id, Double price) {
        Product product = getProductById(id);
        product.setPrice(BigDecimal.valueOf(price));
        product.setLastUpdated(LocalDateTime.now().toString());
        return productRepository.save(product);
    }

    @Override
    public Product getProductByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", barcode));
    }

    @Override
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Override
    public Product toggleProductStatus(String id) {
        Product product = getProductById(id);
        product.setActive(!product.isActive());
        product.setLastUpdated(LocalDateTime.now().toString());
        return productRepository.save(product);
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return productRepository.existsByBarcode(barcode);
    }

    @Override
    public List<Product> getProductsBySupplier(String supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    @Override
    public void updateBulkPrices(List<String> productIds, Double percentageChange) {
        List<Product> products = productRepository.findAllById(productIds);
        products.forEach(product -> {
            BigDecimal currentPrice = product.getPrice();
            BigDecimal changeAmount = currentPrice.multiply(BigDecimal.valueOf(percentageChange / 100));
            product.setPrice(currentPrice.add(changeAmount));
            product.setLastUpdated(LocalDateTime.now().toString());
        });
        productRepository.saveAll(products);
    }

    @Override
    public List<Product> getOutOfStockProducts() {
        return productRepository.findByStockQuantityEquals(0);
    }

    @Override
    public List<Product> getExpiringSoonProducts(int daysThreshold) {
        LocalDateTime thresholdDate = LocalDateTime.now().plusDays(daysThreshold);
        return productRepository.findAll().stream()
                .filter(product -> {
                    if (product.getExpiryDate() != null) {
                        LocalDateTime expiryDate = LocalDateTime.parse(product.getExpiryDate());
                        return expiryDate.isBefore(thresholdDate);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}