package com.virinchi.demo.controller;

import com.virinchi.demo.model.Product;
import com.virinchi.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(homepageController.class)
class HomepageAndCategoriesTest {
    @Autowired MockMvc mvc;
    @MockBean ProductRepository productRepository;

    @Test
    void rootServesHomepage() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("homepage"));
    }

    @Test
    void homepageRequiresLogin_redirects() throws Exception {
        mvc.perform(get("/homepage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?auth=required"));
    }

    @Test
    void productCategoriesPopulatesProducts() throws Exception {
        Product p = new Product(); p.setSku("SKU-A"); p.setName("Quest"); p.setPrice(new BigDecimal("499"));
        Mockito.when(productRepository.findAll()).thenReturn(List.of(p));

        mvc.perform(get("/product_categories"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("products", "productViews"))
                .andExpect(view().name("product_categories"));
    }
}
