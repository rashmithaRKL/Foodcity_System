package com.foodcity.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    private Integer stockQuantity;

    private String imageUrl;
    private String barcode;
    private String supplierId;
    private boolean active = true;
    private BigDecimal discountPrice;
    private String lastUpdated;

    // Additional fields for inventory management
    private Integer minimumStockLevel;
    private Integer reorderPoint;
    private String unit; // e.g., kg, pieces, packets
    private String location; // storage location in warehouse
    private String expiryDate;
}