package com.virinchi.demo.controller;

import com.virinchi.demo.model.signupModel;
import com.virinchi.demo.repository.signupRepo;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Controller
public class signupController {

    @Autowired
    private signupRepo signupRepo;

    @Autowired
    private JavaMailSender mailSender;


        @PostMapping ("/signup")
        public String postSignupPage(@ModelAttribute signupModel lm) {

            String username = lm.getUsername();
            String email = lm.getEmail();
            String password = lm.getPassword();

            String hashPassword = DigestUtils.md5Hex(password.getBytes());
            lm.setPassword(hashPassword);
            signupRepo.save(lm);

            SimpleMailMessage  message= new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Welcome to my Project");
            message.setText("CONGRATULATIONS!!! \n YOU HAVE SUCCESSFULLY SIGNED UP");
            mailSender.send(message);

            return "homepage";
        }

}
