package com.aranthangimart;

import com.aranthangimart.product.Product;
import com.aranthangimart.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest {
    @Autowired MockMvc mvc;
    @Autowired ProductRepository products;

    private String orderJson(String phone, long productId, int quantity) {
        return """
                {"customerName":"Test Customer","phone":"%s","address":"12 Main Street, Aranthangi",
                 "items":[{"productId":%d,"quantity":%d}]}
                """.formatted(phone, productId, quantity);
    }

    private Product anyInStockProduct() {
        return products.findAll().stream().filter(p -> p.getStock() >= 5).findFirst().orElseThrow();
    }

    @Test
    void placingOrderReducesStockAndAddsDeliveryFee() throws Exception {
        Product product = anyInStockProduct();
        int before = product.getStock();

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("9876543210", product.getId(), 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.items", hasSize(1)));

        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(before - 2);
    }

    @Test
    void rejectsInvalidPhoneNumber() throws Exception {
        Product product = anyInStockProduct();
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("12345", product.getId(), 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Enter a valid 10-digit mobile number")));
    }

    @Test
    void rejectsQuantityAboveStockAndLeavesStockUntouched() throws Exception {
        Product product = anyInStockProduct();
        int before = product.getStock();

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("9876543211", product.getId(), 100000)))
                .andExpect(status().isConflict());

        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(before);
    }

    @Test
    void orderLookupRequiresMatchingPhone() throws Exception {
        Product product = anyInStockProduct();
        String response = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("9123456789", product.getId(), 1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = response.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/orders/" + id).param("phone", "9123456789")).andExpect(status().isOk());
        mvc.perform(get("/api/orders/" + id).param("phone", "9000000000")).andExpect(status().isNotFound());
        mvc.perform(get("/api/orders").param("phone", "9123456789")).andExpect(jsonPath("$", hasSize(1)));
    }
}
