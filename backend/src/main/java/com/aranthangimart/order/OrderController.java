package com.aranthangimart.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.aranthangimart.order.OrderDtos.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return service.createOrder(request);
    }

    /** Order history for a customer: GET /api/orders?phone=9876543210 */
    @GetMapping
    public List<OrderResponse> listOrders(@RequestParam String phone) {
        return service.listOrders(phone.trim());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id, @RequestParam String phone) {
        return service.getOrder(id, phone.trim());
    }
}
