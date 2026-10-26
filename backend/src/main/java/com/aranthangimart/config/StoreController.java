package com.aranthangimart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/store")
public class StoreController {
    public record StoreInfo(String name, String tagline, BigDecimal deliveryFee, BigDecimal freeDeliveryAbove) {}

    private final StoreInfo info;

    public StoreController(@Value("${mart.name}") String name,
                           @Value("${mart.tagline}") String tagline,
                           DeliveryPolicy delivery) {
        this.info = new StoreInfo(name, tagline, delivery.getFee(), delivery.getFreeAbove());
    }

    @GetMapping
    public StoreInfo getStoreInfo() {
        return info;
    }
}
