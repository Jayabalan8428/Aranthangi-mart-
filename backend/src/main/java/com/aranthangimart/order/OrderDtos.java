package com.aranthangimart.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}

    public record CreateOrderRequest(
            @NotBlank(message = "Name is required") @Size(max = 80, message = "Name is too long") String customerName,
            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number") String phone,
            @NotBlank(message = "Delivery address is required")
            @Size(max = 300, message = "Address is too long") String address,
            @NotEmpty(message = "Your cart is empty") List<@Valid OrderLineRequest> items) {}

    public record OrderLineRequest(
            @NotNull(message = "Product is required") Long productId,
            @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {}

    public record OrderResponse(
            Long id,
            String customerName,
            String phone,
            String address,
            String status,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal total,
            Instant createdAt,
            List<OrderLineResponse> items) {}

    public record OrderLineResponse(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice) {}
}
