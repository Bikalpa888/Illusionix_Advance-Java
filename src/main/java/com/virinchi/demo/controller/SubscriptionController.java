package com.virinchi.demo.controller;

import com.virinchi.demo.model.NewsletterSubscription;
import com.virinchi.demo.repository.NewsletterSubscriptionRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SubscriptionController {

    private final NewsletterSubscriptionRepository repo;
    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String notifyTo;

    public SubscriptionController(NewsletterSubscriptionRepository repo,
                                  JavaMailSender mailSender,
                                  @Value("${spring.mail.username:}") String mailFrom,
                                  @Value("${app.contact.notifyTo:}") String notifyTo) {
        this.repo = repo;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.notifyTo = (notifyTo == null || notifyTo.isBlank()) ? mailFrom : notifyTo;
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam("email") @NotBlank @Email String email) {
        String norm = email.trim().toLowerCase();
        if (norm.isEmpty()) return "redirect:/homepage?sub=invalid";
        if (repo.existsByEmail(norm)) return "redirect:/homepage?sub=exists";
        NewsletterSubscription s = new NewsletterSubscription();
        s.setEmail(norm);
        repo.save(s);

        try {
            if (mailSender != null && notifyTo != null && !notifyTo.isBlank()) {
                SimpleMailMessage msg = new SimpleMailMessage();
                if (mailFrom != null && !mailFrom.isBlank()) msg.setFrom(mailFrom);
                msg.setTo(notifyTo);
                msg.setSubject("New Newsletter Subscriber");
                msg.setText("Email: " + norm);
                mailSender.send(msg);
            }
        } catch (Exception ignored) {}

        return "redirect:/homepage?sub=ok";
    }
}

