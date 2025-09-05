package com.virinchi.demo.controller;

import com.virinchi.demo.model.signupModel;
import com.virinchi.demo.repository.signupRepo;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class signupController {

    private final signupRepo signupRepo;
    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String notifyTo;

    public signupController(signupRepo signupRepo,
                            JavaMailSender mailSender,
                            @Value("${spring.mail.username:}") String mailFrom,
                            @Value("${app.contact.notifyTo:}") String notifyTo) {
        this.signupRepo = signupRepo;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.notifyTo = (notifyTo == null || notifyTo.isBlank()) ? mailFrom : notifyTo;
    }

    @PostMapping("/signup")
    public String postSignupPage(@ModelAttribute signupModel lm) {
        // normalize email/username; avoid brittle duplicate prechecks
        if (lm.getEmail() != null) lm.setEmail(lm.getEmail().trim().toLowerCase());
        if (lm.getUsername() != null) lm.setUsername(lm.getUsername().trim());
        // hash with MD5 to keep existing behavior
        String hashPassword = DigestUtils.md5Hex(lm.getPassword().getBytes());
        lm.setPassword(hashPassword);
        try {
            signupRepo.save(lm);
        } catch (Exception e) {
            // If DB rejects (e.g., legacy unique constraint), bounce back with a hint
            return "redirect:/signup?error=save";
        }

        // Notify admin and/or user — use configured sender to avoid Gmail rejection
        try {
            if (mailSender != null && notifyTo != null && !notifyTo.isBlank()) {
                SimpleMailMessage adminMsg = new SimpleMailMessage();
                if (mailFrom != null && !mailFrom.isBlank()) adminMsg.setFrom(mailFrom);
                adminMsg.setTo(notifyTo);
                adminMsg.setSubject("New user signup");
                adminMsg.setText("Username: " + lm.getUsername() + "\nEmail: " + lm.getEmail());
                mailSender.send(adminMsg);
            }
        } catch (Exception ignored) {}

        // Optional welcome email to user (best-effort, ignore errors)
        try {
            if (mailSender != null && lm.getEmail() != null && !lm.getEmail().isBlank()) {
                SimpleMailMessage message = new SimpleMailMessage();
                if (mailFrom != null && !mailFrom.isBlank()) message.setFrom(mailFrom);
                message.setTo(lm.getEmail());
                message.setSubject("Welcome to Illusionix");
                message.setText("Hi " + (lm.getUsername() == null ? "there" : lm.getUsername()) + ",\n\nThanks for signing up to Illusionix!\n\n— Team Illusionix");
                mailSender.send(message);
            }
        } catch (Exception ignored) {}

        return "redirect:/login";
    }
}
