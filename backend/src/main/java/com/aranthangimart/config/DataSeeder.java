package com.aranthangimart.config;

import com.aranthangimart.product.Product;
import com.aranthangimart.product.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository products) {
        return args -> {
            if (products.count() > 0) return;
            products.saveAll(List.of(
                // Groceries
                p("Ponni Raw Rice", "Everyday raw rice, perfect for meals and idli batter.", "Groceries", "5 kg", "320", 40, "🍚"),
                p("Toor Dal", "Protein-rich dal for sambar and kootu.", "Groceries", "1 kg", "165", 35, "🫘"),
                p("Sunflower Oil", "Refined oil for everyday cooking.", "Groceries", "1 L", "135", 50, "🌻"),
                p("Gingelly (Nallennai) Oil", "Cold-pressed sesame oil with a rich aroma.", "Groceries", "500 ml", "260", 20, "🫙"),
                p("Sambar Powder", "Fresh-ground blend of spices for sambar.", "Groceries", "200 g", "85", 30, "🌶️"),
                p("Filter Coffee Powder", "80:20 coffee-chicory blend for strong South Indian filter kaapi.", "Groceries", "250 g", "190", 25, "☕"),
                p("Jaggery (Vellam)", "Natural jaggery for sweets and tea.", "Groceries", "500 g", "60", 4, "🟤"),
                // Vegetables
                p("Tomato", "Fresh, firm country tomatoes.", "Vegetables", "1 kg", "40", 60, "🍅"),
                p("Small Onion (Sambar Onion)", "Shallots for sambar, kuzhambu and poriyal.", "Vegetables", "500 g", "60", 45, "🧅"),
                p("Drumstick", "Tender moringa pods, great for sambar.", "Vegetables", "250 g", "35", 3, "🌿"),
                // Fruits
                p("Banana (Robusta)", "Ripe, sweet bananas.", "Fruits", "1 dozen", "60", 30, "🍌"),
                p("Coconut", "Fresh coconut, medium size.", "Fruits", "1 piece", "30", 0, "🥥"),
                // Dairy
                p("Full Cream Milk", "Fresh pasteurised milk.", "Dairy", "500 ml", "30", 80, "🥛"),
                p("Curd", "Thick, set curd made fresh.", "Dairy", "500 g", "35", 40, "🍶"),
                // Snacks
                p("Murukku", "Crispy rice-flour murukku, tea-time favourite.", "Snacks", "200 g", "70", 25, "🥨"),
                p("Banana Chips", "Kerala-style banana chips fried in coconut oil.", "Snacks", "200 g", "80", 25, "🍌"),
                // Household
                p("Dish Wash Liquid", "Lemon dish wash liquid with strong grease cutting.", "Household", "500 ml", "99", 30, "🧼"),
                p("Floor Cleaner", "Disinfectant floor cleaner with fresh fragrance.", "Household", "1 L", "145", 20, "🧽"),
                // Personal care
                p("Sandal Soap (Pack of 4)", "Classic sandalwood bathing soap.", "Personal Care", "4 x 100 g", "150", 30, "🧴")
            ));
        };
    }

    private static Product p(String name, String description, String category, String unit,
                             String price, int stock, String emoji) {
        return new Product(name, description, category, unit, new BigDecimal(price), stock, emoji);
    }
}
