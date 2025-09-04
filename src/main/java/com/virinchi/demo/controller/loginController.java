package com.virinchi.demo.controller;

import com.virinchi.demo.model.signupModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.virinchi.demo.repository.signupRepo;
import jakarta.servlet.http.HttpSession;

@Controller
public class loginController {

    @Autowired
    private signupRepo signupRepo;


    @PostMapping("/login")
    public String loginPage(@ModelAttribute signupModel lm, Model m, HttpSession session)
    {
        String username = lm.getUsername();
        String password = lm.getPassword();

        String hashPassword = DigestUtils.md5Hex(password.getBytes());

        boolean result=signupRepo.existsByUsernameAndPassword(username, hashPassword);
        if(result==true)
        {
            session.setAttribute("activeUser", username);
            session.setMaxInactiveInterval(20);
            m.addAttribute("uList", signupRepo.findAll());
            return "homepage";
        }
        else
        {
            m.addAttribute("loginerror","Username or Password Incorrect");
            return "login";
        }
    }


}
