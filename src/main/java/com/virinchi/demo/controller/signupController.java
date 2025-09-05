package com.virinchi.demo.controller;

import com.virinchi.demo.model.signupModel;
import com.virinchi.demo.repository.signupRepo;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class signupController {

    @Autowired private signupRepo signupRepo;
    @Autowired private JavaMailSender mailSender;

    @PostMapping("/signup")
    public String postSignupPage(@ModelAttribute signupModel lm) {
        // hash with MD5
        String hashPassword = DigestUtils.md5Hex(lm.getPassword().getBytes());
        lm.setPassword(hashPassword);
        signupRepo.save(lm);

        // try sending email, ignore failures
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("YOUR_GMAIL@gmail.com"); // must match spring.mail.username
            message.setTo(lm.getEmail());
            message.setSubject("Welcome to Illusionix");
            message.setText("You have successfully signed up.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Mail send failed: " + e.getMessage());
        }

        return "redirect:/login";
    }
}
