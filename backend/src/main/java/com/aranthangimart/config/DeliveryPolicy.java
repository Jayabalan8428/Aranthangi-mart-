package com.aranthangimart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Delivery charge rules. The backend is the single source of truth; the UI only displays them. */
@Component
public class DeliveryPolicy {
    private final BigDecimal fee;
    private final BigDecimal freeAbove;

    public DeliveryPolicy(@Value("${mart.delivery.fee}") BigDecimal fee,
                          @Value("${mart.delivery.free-above}") BigDecimal freeAbove) {
        this.fee = fee;
        this.freeAbove = freeAbove;
    }

    public BigDecimal feeFor(BigDecimal subtotal) {
        return subtotal.compareTo(freeAbove) >= 0 ? BigDecimal.ZERO : fee;
    }

    public BigDecimal getFee() { return fee; }
    public BigDecimal getFreeAbove() { return freeAbove; }
}
