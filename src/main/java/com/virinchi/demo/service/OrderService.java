package com.virinchi.demo.service;

import com.virinchi.demo.model.CartItem;
import com.virinchi.demo.model.Order;
import com.virinchi.demo.model.OrderItem;
import com.virinchi.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    public Order place(String ownerKey, String userName, String userEmail) {
        List<CartItem> items = cartService.list(ownerKey);
        BigDecimal subtotal = items.stream()
                .map(ci -> ci.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subtotal; // taxes/shipping can be added later

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserName(userName);
        order.setUserEmail(userEmail);
        order.setSubtotal(subtotal);
        order.setTotal(total);
        order.setStatus("Placed");
        order.setCreatedAt(LocalDateTime.now());

        for (CartItem ci : items) {
            OrderItem oi = new OrderItem();
            oi.setProductSku(ci.getProductSku());
            oi.setName(ci.getName());
            oi.setPrice(ci.getPrice());
            oi.setQuantity(ci.getQuantity());
            order.addItem(oi);
        }

        Order saved = orderRepository.save(order);
        cartService.clear(ownerKey);
        return saved;
    }

    private String generateOrderNumber(){
        String rnd = UUID.randomUUID().toString().substring(0,8).toUpperCase();
        return "VR-" + java.time.Year.now().getValue() + "-" + rnd;
    }
}

