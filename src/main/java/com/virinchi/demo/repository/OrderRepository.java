package com.virinchi.demo.repository;

import com.virinchi.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findTop50ByOrderByCreatedAtDesc();
    List<Order> findTop50ByUserNameOrderByCreatedAtDesc(String userName);
    List<Order> findTop50ByUserEmailOrderByCreatedAtDesc(String userEmail);

    @Query(value = "SELECT order_number AS orderNumber, user_name AS userName, user_email AS userEmail, IFNULL(CAST(total AS CHAR), '') AS totalStr, status AS status, CASE WHEN created_at = '0000-00-00 00:00:00' OR created_at IS NULL THEN NULL ELSE DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') END AS createdAtStr FROM orders", nativeQuery = true)
    java.util.List<OrderExportView> findAllForExport();
}
