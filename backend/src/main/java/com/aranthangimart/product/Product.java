package com.aranthangimart.product;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1200)
    private String description;

    @Column(nullable = false)
    private String category;

    /** Pack size shown to the customer, e.g. "1 kg", "500 ml", "1 dozen". */
    @Column(nullable = false)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private String emoji;

    protected Product() {}

    public Product(String name, String description, String category, String unit,
                   BigDecimal price, int stock, String emoji) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.unit = unit;
        this.price = price;
        this.stock = stock;
        this.emoji = emoji;
    }

    public void reduceStock(int quantity) {
        if (quantity > stock) {
            throw new IllegalStateException("Insufficient stock for " + name);
        }
        stock -= quantity;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getEmoji() { return emoji; }
}
