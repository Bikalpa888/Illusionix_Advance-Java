package com.virinchi.demo.controller;

import com.virinchi.demo.model.signupModel;
import com.virinchi.demo.repository.signupRepo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(loginController.class)
class LoginControllerTest {

    @Autowired MockMvc mvc;
    @MockBean signupRepo signupRepo;

    @Test
    void adminLoginSuccess_redirectsToAdminDashboard_andSetsSession() throws Exception {
        mvc.perform(post("/login")
                .param("username", "admin@illusionix.com")
                .param("password", "user@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_dashboard"))
                .andExpect(request().sessionAttribute("isAdmin", true))
                .andExpect(request().sessionAttribute("adminEmail", "admin@illusionix.com"))
                .andExpect(request().sessionAttribute("activeUser", "ADMIN"));
    }

    @Test
    void userLoginSuccess_redirectsHome_andStoresNameEmail() throws Exception {
        // Mock user repo
        Mockito.when(signupRepo.existsByUsernameAndPassword(Mockito.eq("alice"), Mockito.anyString()))
                .thenReturn(true);
        signupModel m = new signupModel();
        m.setUsername("alice"); m.setEmail("alice@example.com");
        Mockito.when(signupRepo.findByUsername("alice")).thenReturn(m);

        mvc.perform(post("/login").param("username", "alice").param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage"))
                .andExpect(request().sessionAttribute("activeUser", "alice"))
                .andExpect(request().sessionAttribute("userName", "alice"))
                .andExpect(request().sessionAttribute("userEmail", "alice@example.com"));
    }

    @Test
    void loginFailure_returnsLoginViewWithError() throws Exception {
        Mockito.when(signupRepo.existsByUsernameAndPassword(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(false);
        mvc.perform(post("/login").param("username", "bob").param("password", "bad"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginerror"));
    }
}

