package com.virinchi.demo.controller;

import com.virinchi.demo.repository.CartItemRepository;
import com.virinchi.demo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartAndOrderFlowTest {

    @Autowired MockMvc mvc;
    @Autowired CartItemRepository cartRepo;
    @Autowired OrderRepository orderRepo;
    @MockBean JavaMailSender mailSender; // avoid real emails

    @Test
    void addToCart_placeOrder_clearsCart_andPersistsOrder() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userName", "alice");
        session.setAttribute("userEmail", "alice@example.com");

        // Add two items
        mvc.perform(post("/cart/add").session(session)
                .param("sku","SKU-1").param("qty","2").param("name","Meta Quest 3").param("price","499"))
                .andExpect(status().isOk());
        mvc.perform(post("/cart/add").session(session)
                .param("sku","SKU-2").param("qty","1").param("name","VR Controller").param("price","149"))
                .andExpect(status().isOk());

        // Verify cart summary
        mvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));

        // Place order
        mvc.perform(post("/order/place").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.orderNumber").exists());

        // Cart cleared
        assertThat(cartRepo.findByOwnerKeyOrderByCreatedAtAsc("user:alice")).isEmpty();
        // Order persisted
        assertThat(orderRepo.findTop50ByUserEmailOrderByCreatedAtDesc("alice@example.com")).isNotEmpty();
    }
}

