package com.aranthangimart.product;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    /**
     * sort: default | priceAsc | priceDesc | name
     */
    @GetMapping
    public List<Product> getProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "default") String sort) {
        String cat = "All".equalsIgnoreCase(category.trim()) ? "" : category.trim();
        return products.filter(search.trim(), cat, sortFor(sort));
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return products.findCategories();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    private Sort sortFor(String sort) {
        return switch (sort) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };
    }
}
