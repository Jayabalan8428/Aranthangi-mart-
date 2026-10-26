package com.aranthangimart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductApiTest {
    @Autowired MockMvc mvc;

    @Test
    void listsSeededProducts() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(5))));
    }

    @Test
    void searchIsCaseInsensitive() throws Exception {
        mvc.perform(get("/api/products").param("search", "TOOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", containsStringIgnoringCase("toor")));
    }

    @Test
    void filtersByCategoryAndSortsByPrice() throws Exception {
        mvc.perform(get("/api/products").param("category", "Dairy").param("sort", "priceAsc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", everyItem(is("Dairy"))));
    }

    @Test
    void exposesCategoriesAndStoreInfo() throws Exception {
        mvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("Groceries")));
        mvc.perform(get("/api/store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Aranthangi Mart")));
    }

    @Test
    void unknownProductReturns404() throws Exception {
        mvc.perform(get("/api/products/999999")).andExpect(status().isNotFound());
    }
}
