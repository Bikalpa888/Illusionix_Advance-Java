package com.virinchi.demo.service;

import com.virinchi.demo.model.CartItem;
import com.virinchi.demo.model.Product;
import com.virinchi.demo.repository.CartItemRepository;
import com.virinchi.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> list(String ownerKey) {
        return cartItemRepository.findByOwnerKeyOrderByCreatedAtAsc(ownerKey);
    }

    public CartItem add(String ownerKey, String productSku, Integer qty, String name, BigDecimal price, String image) {
        var existing = cartItemRepository.findByOwnerKeyAndProductSku(ownerKey, productSku).orElse(null);
        if (existing != null) {
            existing.setQuantity(Math.max(1, existing.getQuantity() + (qty == null ? 1 : qty)));
            return cartItemRepository.save(existing);
        }

        // If product exists in DB, trust DB values where available
        Product p = productRepository.findBySku(productSku).orElse(null);
        CartItem item = new CartItem();
        item.setOwnerKey(ownerKey);
        item.setProductSku(productSku);
        item.setQuantity(qty == null ? 1 : Math.max(1, qty));
        item.setName(p != null ? p.getName() : (name != null ? name : productSku));
        item.setPrice(p != null && p.getPrice() != null ? p.getPrice() : (price != null ? price : BigDecimal.ZERO));
        item.setImage(image);
        return cartItemRepository.save(item);
    }

    public CartItem updateQty(String ownerKey, String productSku, Integer qty) {
        var item = cartItemRepository.findByOwnerKeyAndProductSku(ownerKey, productSku)
                .orElseThrow(() -> new IllegalArgumentException("Item not in cart"));
        item.setQuantity(Math.max(1, qty == null ? 1 : qty));
        return cartItemRepository.save(item);
    }

    public void remove(String ownerKey, String productSku) {
        cartItemRepository.deleteByOwnerKeyAndProductSku(ownerKey, productSku);
    }

    public void clear(String ownerKey) {
        cartItemRepository.deleteByOwnerKey(ownerKey);
    }

    public Map<String, Object> summary(String ownerKey) {
        var items = list(ownerKey);
        BigDecimal subtotal = items.stream()
                .map(ci -> ci.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = items.stream().mapToInt(ci -> ci.getQuantity()).sum();
        Map<String, Object> m = new HashMap<>();
        m.put("count", count);
        m.put("subtotal", subtotal);
        m.put("items", items);
        return m;
    }
}

