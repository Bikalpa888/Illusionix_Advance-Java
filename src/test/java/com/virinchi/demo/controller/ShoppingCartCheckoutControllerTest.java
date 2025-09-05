package com.virinchi.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShoppingCartCheckoutControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void checkoutViewContainsServerCart() throws Exception {
        MockHttpSession session = new MockHttpSession();
        // Put one item in cart first
        mvc.perform(post("/cart/add").session(session)
                .param("sku","SKU-TEST").param("qty","1").param("name","Item").param("price","10"))
                .andExpect(status().isOk());

        mvc.perform(get("/shopping_cart_checkout").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("serverCart", "serverCartJson", "cartCount", "cartSubtotal"))
                .andExpect(view().name("shopping_cart_checkout"));
    }
}

