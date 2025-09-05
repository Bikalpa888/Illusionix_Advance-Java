package com.virinchi.demo.controller;

import com.virinchi.demo.model.Order;
import com.virinchi.demo.model.OrderItem;
import com.virinchi.demo.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDashboardOrdersTest {

    @Autowired MockMvc mvc;
    @Autowired OrderRepository orderRepo;

    @BeforeEach
    void seedOrder(){
        if(orderRepo.findTop50ByOrderByCreatedAtDesc().isEmpty()){
            Order o = new Order();
            o.setOrderNumber("VR-TEST-0001");
            o.setUserName("seed");
            o.setUserEmail("seed@example.com");
            o.setSubtotal(new BigDecimal("10"));
            o.setTotal(new BigDecimal("10"));
            OrderItem it = new OrderItem();
            it.setName("Test Item"); it.setProductSku("SKU-SEED"); it.setPrice(new BigDecimal("10")); it.setQuantity(1);
            o.addItem(it);
            orderRepo.save(o);
        }
    }

    @Test
    void adminDashboardShowsRecentOrders() throws Exception {
        MockHttpSession admin = new MockHttpSession();
        admin.setAttribute("isAdmin", true);
        mvc.perform(get("/admin_dashboard").session(admin))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("orders"))
                .andExpect(view().name("admin_dashboard"));
    }
}

