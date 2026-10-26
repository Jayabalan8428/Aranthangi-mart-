package com.aranthangimart.order;

import com.aranthangimart.config.DeliveryPolicy;
import com.aranthangimart.product.Product;
import com.aranthangimart.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.aranthangimart.order.OrderDtos.*;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final DeliveryPolicy delivery;

    public OrderService(OrderRepository orders, ProductRepository products, DeliveryPolicy delivery) {
        this.orders = orders;
        this.products = products;
        this.delivery = delivery;
    }

    /** Prices and stock always come from the database, never from the browser. */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Merge duplicate product lines so stock is checked against the total quantity.
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderLineRequest line : request.items()) {
            quantities.merge(line.productId(), line.quantity(), Integer::sum);
        }

        OrderEntity order = new OrderEntity(
                request.customerName().trim(), request.phone().trim(), request.address().trim());
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = products.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Unknown product id: " + productId));

            if (product.getStock() < quantity) {
                String message = product.getStock() == 0
                        ? product.getName() + " is out of stock"
                        : "Only " + product.getStock() + " left of " + product.getName();
                throw new ResponseStatusException(HttpStatus.CONFLICT, message);
            }

            product.reduceStock(quantity);
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            order.addItem(new OrderItem(product.getId(), product.getName(), quantity, product.getPrice()));
        }

        order.setTotals(subtotal, delivery.feeFor(subtotal));
        return toResponse(orders.save(order));
    }

    /** The phone number acts as a lightweight proof of ownership until real login is added. */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id, String phone) {
        OrderEntity order = orders.findByIdAndPhone(id, phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(String phone) {
        return orders.findByPhoneOrderByCreatedAtDesc(phone).stream().map(this::toResponse).toList();
    }

    private OrderResponse toResponse(OrderEntity order) {
        List<OrderLineResponse> lines = order.getItems().stream()
                .map(item -> new OrderLineResponse(item.getProductId(), item.getProductName(),
                        item.getQuantity(), item.getUnitPrice()))
                .toList();
        return new OrderResponse(order.getId(), order.getCustomerName(), order.getPhone(),
                order.getAddress(), order.getStatus().name(), order.getSubtotal(),
                order.getDeliveryFee(), order.getTotal(), order.getCreatedAt(), lines);
    }
}
