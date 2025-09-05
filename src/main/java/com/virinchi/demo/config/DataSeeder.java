package com.virinchi.demo.config;

import com.virinchi.demo.model.Product;
import com.virinchi.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedProducts(ProductRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                Product a = new Product();
                a.setSku("MQ3-128");
                a.setName("Meta Quest 3 - 128GB");
                a.setPrice(new BigDecimal("549.00"));
                a.setInventory(42);
                a.setStatus("Active");
                a.setDescription("Starter VR headset with great value.");
                repo.save(a);

                Product b = new Product();
                b.setSku("VRC-PRO");
                b.setName("VR Controller Pro");
                b.setPrice(new BigDecimal("149.00"));
                b.setInventory(120);
                b.setStatus("Active");
                b.setDescription("Precision controller for immersive gaming.");
                repo.save(b);
            }
        };
    }
}

