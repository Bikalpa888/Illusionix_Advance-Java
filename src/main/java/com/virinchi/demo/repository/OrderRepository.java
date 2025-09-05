package com.virinchi.demo.repository;

import com.virinchi.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findTop50ByOrderByCreatedAtDesc();
    List<Order> findTop50ByUserNameOrderByCreatedAtDesc(String userName);
    List<Order> findTop50ByUserEmailOrderByCreatedAtDesc(String userEmail);
}
