package com.virinchi.demo.controller;

import com.virinchi.demo.repository.ContactMessageRepository;
import com.virinchi.demo.repository.NewsletterSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({contactController.class, SubscriptionController.class})
class ContactAndSubscribeTest {
    @Autowired MockMvc mvc;
    @MockBean ContactMessageRepository contactRepo;
    @MockBean NewsletterSubscriptionRepository subRepo;
    @MockBean JavaMailSender mailSender;

    @Test
    void contactValidSavesAndRedirects() throws Exception {
        mvc.perform(post("/contact")
                        .param("firstName","Alice")
                        .param("lastName","Doe")
                        .param("email","alice@example.com")
                        .param("phone","123")
                        .param("subject","Help")
                        .param("message","Hi")
                        .param("privacy","true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contact?sent=1"));
        Mockito.verify(contactRepo, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void contactInvalidRedirectsInvalid() throws Exception {
        mvc.perform(post("/contact")
                        .param("firstName","") // invalid
                        .param("privacy","false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contact?invalid=1"));
        Mockito.verify(contactRepo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void subscribeNew_ok() throws Exception {
        Mockito.when(subRepo.existsByEmail("news@x.com")).thenReturn(false);
        mvc.perform(post("/subscribe").param("email","news@x.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage?sub=ok"));
        ArgumentCaptor<com.virinchi.demo.model.NewsletterSubscription> cap = ArgumentCaptor.forClass(com.virinchi.demo.model.NewsletterSubscription.class);
        Mockito.verify(subRepo).save(cap.capture());
        assertThat(cap.getValue().getEmail()).isEqualTo("news@x.com");
    }

    @Test
    void subscribeExists_redirectsExists() throws Exception {
        Mockito.when(subRepo.existsByEmail("dup@x.com")).thenReturn(true);
        mvc.perform(post("/subscribe").param("email","dup@x.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage?sub=exists"));
        Mockito.verify(subRepo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void subscribeInvalid_redirectsInvalid() throws Exception {
        mvc.perform(post("/subscribe").param("email","bad"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage?sub=invalid"));
    }
}

