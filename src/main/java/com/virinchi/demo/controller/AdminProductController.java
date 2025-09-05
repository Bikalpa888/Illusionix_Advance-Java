package com.virinchi.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virinchi.demo.model.Product;
import com.virinchi.demo.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostMapping("/products")
    public String addProduct(@RequestParam("sku") String sku,
                             @RequestParam("name") String name,
                             @RequestParam(value = "price", required = false) String priceStr,
                             @RequestParam(value = "inventory", required = false) Integer inventory,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "description", required = false) String description,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images,
                             HttpSession session,
                             Model model) {
        Object isAdmin = session.getAttribute("isAdmin");
        if (!(isAdmin instanceof Boolean) || !((Boolean) isAdmin)) {
            return "redirect:/login?admin=required";
        }

        if (sku == null || sku.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "SKU and Name are required");
            return "redirect:/admin_dashboard?error=missing_fields";
        }

        Product p = productRepository.findBySku(sku).orElse(new Product());
        p.setSku(sku.trim());
        p.setName(name.trim());

        try {
            if (priceStr != null && !priceStr.isBlank()) {
                p.setPrice(new BigDecimal(priceStr));
            }
        } catch (Exception ignored) {}

        p.setInventory(inventory);
        p.setStatus(status);
        p.setDescription(description);

        List<String> encoded = new ArrayList<>();
        if (images != null) {
            int count = 0;
            for (MultipartFile f : images) {
                if (f == null || f.isEmpty()) continue;
                try {
                    String mime = f.getContentType() != null ? f.getContentType() : "image/*";
                    String base64 = Base64.getEncoder().encodeToString(f.getBytes());
                    encoded.add("data:" + mime + ";base64," + base64);
                    count++;
                    if (count >= 6) break;
                } catch (IOException ignored) {}
            }
        }
        if (!encoded.isEmpty()) {
            try {
                p.setImagesJson(objectMapper.writeValueAsString(encoded));
            } catch (JsonProcessingException ignored) {}
        }

        productRepository.save(p);
        return "redirect:/admin_dashboard?added";
    }
}

