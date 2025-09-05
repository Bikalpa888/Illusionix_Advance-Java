package com.virinchi.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virinchi.demo.model.Product;
import com.virinchi.demo.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductApiController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        List<Product> all = productRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Product p : all) {
            response.add(toDto(p));
        }
        return response;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> payload) {
        try {
            String sku = String.valueOf(payload.getOrDefault("sku", "")).trim();
            String name = String.valueOf(payload.getOrDefault("name", "")).trim();
            if (sku.isEmpty() || name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "SKU and name are required"));
            }
            if (productRepository.existsBySku(sku)) {
                // idempotent upsert by SKU
                Product existing = productRepository.findBySku(sku).orElseThrow();
                applyPayload(existing, payload);
                productRepository.save(existing);
                return ResponseEntity.ok(toDto(existing));
            }

            Product p = new Product();
            p.setSku(sku);
            p.setName(name);
            applyPayload(p, payload);
            productRepository.save(p);
            return ResponseEntity.ok(toDto(p));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private void applyPayload(Product p, Map<String, Object> payload) {
        Object priceObj = payload.get("price");
        if (priceObj != null) {
            try {
                p.setPrice(new BigDecimal(String.valueOf(priceObj)));
            } catch (Exception ignored) {}
        }
        Object invObj = payload.get("inventory");
        if (invObj != null) {
            try {
                p.setInventory(Integer.parseInt(String.valueOf(invObj)));
            } catch (Exception ignored) {}
        }
        Object status = payload.get("status");
        if (status != null) p.setStatus(String.valueOf(status));
        Object desc = payload.get("description");
        if (desc != null) p.setDescription(String.valueOf(desc));

        Object images = payload.get("images");
        if (images instanceof List<?> list) {
            try {
                p.setImagesJson(objectMapper.writeValueAsString(list));
            } catch (Exception ignored) {}
        } else if (images instanceof String s) {
            p.setImagesJson(s);
        }
    }

    private Map<String, Object> toDto(Product p) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", p.getId());
        dto.put("sku", p.getSku());
        dto.put("name", p.getName());
        dto.put("price", p.getPrice());
        dto.put("inventory", p.getInventory());
        dto.put("status", p.getStatus());
        dto.put("description", p.getDescription());
        List<String> images = new ArrayList<>();
        if (p.getImagesJson() != null && !p.getImagesJson().isBlank()) {
            try {
                images = objectMapper.readValue(p.getImagesJson(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                images = List.of(p.getImagesJson());
            }
        }
        dto.put("images", images);
        return dto;
    }
}

