package com.virinchi.demo.repository;

import com.virinchi.demo.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByOwnerKeyOrderByCreatedAtAsc(String ownerKey);
    Optional<CartItem> findByOwnerKeyAndProductSku(String ownerKey, String productSku);
    void deleteByOwnerKey(String ownerKey);
    void deleteByOwnerKeyAndProductSku(String ownerKey, String productSku);
}

