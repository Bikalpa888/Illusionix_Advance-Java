package com.virinchi.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virinchi.demo.model.CartItem;
import com.virinchi.demo.model.Order;
import com.virinchi.demo.service.CartService;
import com.virinchi.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Controller
public class shoppingCartCheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String notifyTo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public shoppingCartCheckoutController(CartService cartService,
                                          OrderService orderService,
                                          JavaMailSender mailSender,
                                          @Value("${spring.mail.username:}") String mailFrom,
                                          @Value("${app.contact.notifyTo:}") String notifyTo) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.notifyTo = (notifyTo == null || notifyTo.isBlank()) ? mailFrom : notifyTo;
    }

    private String ownerKey(HttpSession session){
        Object name = session.getAttribute("userName");
        if(name instanceof String && !((String) name).isBlank()) return "user:" + name;
        Object active = session.getAttribute("activeUser");
        if(active instanceof String && !((String) active).isBlank()) return "user:" + active;
        return "session:" + session.getId();
    }

    @PostMapping("/order/place")
    @ResponseBody
    public Map<String,Object> placeOrder(HttpSession session){
        String key = ownerKey(session);
        String userName = objToStr(session.getAttribute("userName"));
        if(userName == null || userName.isBlank()) userName = objToStr(session.getAttribute("activeUser"));
        String userEmail = objToStr(session.getAttribute("userEmail"));
        Order order = orderService.place(key, userName, userEmail);
        sendOrderEmails(order);
        return Map.of(
                "ok", true,
                "orderNumber", order.getOrderNumber(),
                "total", order.getTotal()
        );
    }

    private String objToStr(Object o){ return (o instanceof String) ? (String) o : null; }

    private void sendOrderEmails(Order order){
        try {
            if (mailSender != null) {
                // Admin notification
                if (notifyTo != null && !notifyTo.isBlank()) {
                    SimpleMailMessage admin = new SimpleMailMessage();
                    if (mailFrom != null && !mailFrom.isBlank()) admin.setFrom(mailFrom);
                    admin.setTo(notifyTo);
                    admin.setSubject("New Order: " + order.getOrderNumber());
                    admin.setText(buildAdminBody(order));
                    mailSender.send(admin);
                }
                // User confirmation
                if (order.getUserEmail() != null && !order.getUserEmail().isBlank()) {
                    SimpleMailMessage user = new SimpleMailMessage();
                    if (mailFrom != null && !mailFrom.isBlank()) user.setFrom(mailFrom);
                    user.setTo(order.getUserEmail());
                    user.setSubject("Your Order " + order.getOrderNumber() + " is Confirmed");
                    user.setText(buildUserBody(order));
                    mailSender.send(user);
                }
            }
        } catch (Exception ignored) {}
    }

    private String buildAdminBody(Order o){
        StringBuilder sb = new StringBuilder();
        sb.append("Order Number: ").append(o.getOrderNumber()).append('\n');
        sb.append("Customer: ").append(o.getUserName()).append(" <").append(o.getUserEmail()).append(">").append('\n');
        sb.append("Total: ").append(o.getTotal()).append('\n');
        sb.append("Items:\n");
        o.getItems().forEach(it -> sb.append(" - ").append(it.getName())
                .append(" x").append(it.getQuantity())
                .append(" @ ").append(it.getPrice()).append('\n'));
        return sb.toString();
    }

    private String buildUserBody(Order o){
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(o.getUserName() == null ? "there" : o.getUserName()).append(",\n\n");
        sb.append("Thanks for your purchase! Your order ").append(o.getOrderNumber()).append(" has been placed.\n");
        sb.append("Order total: ").append(o.getTotal()).append("\n\n");
        sb.append("Items:\n");
        o.getItems().forEach(it -> sb.append(" - ").append(it.getName())
                .append(" x").append(it.getQuantity())
                .append(" @ ").append(it.getPrice()).append('\n'));
        sb.append("\nWe will notify you when your order ships.\n\n— Illusionix");
        return sb.toString();
    }
    @GetMapping("/shopping_cart_checkout")
    public String checkout(Model model, HttpSession session) throws JsonProcessingException {
        String key = ownerKey(session);
        Map<String,Object> cart = cartService.summary(key);
        model.addAttribute("serverCart", cart);
        model.addAttribute("cartCount", cart.get("count"));
        model.addAttribute("cartSubtotal", cart.get("subtotal"));
        model.addAttribute("serverCartJson", objectMapper.writeValueAsString(cart));
        return "shopping_cart_checkout";
    }

    @GetMapping("/cart")
    @ResponseBody
    public Map<String,Object> getCart(HttpSession session){
        return cartService.summary(ownerKey(session));
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public CartItem addItem(HttpSession session,
                            @RequestParam("sku") String sku,
                            @RequestParam(value = "qty", required = false) Integer qty,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "price", required = false) BigDecimal price,
                            @RequestParam(value = "image", required = false) String image){
        return cartService.add(ownerKey(session), sku, qty, name, price, image);
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public CartItem updateItem(HttpSession session,
                               @RequestParam("sku") String sku,
                               @RequestParam("qty") Integer qty){
        return cartService.updateQty(ownerKey(session), sku, qty);
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeItem(HttpSession session,
                                        @RequestParam("sku") String sku){
        cartService.remove(ownerKey(session), sku);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(HttpSession session){
        cartService.clear(ownerKey(session));
        return ResponseEntity.ok().build();
    }
}
