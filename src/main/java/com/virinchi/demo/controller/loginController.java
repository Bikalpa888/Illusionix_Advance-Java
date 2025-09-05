package com.virinchi.demo.controller;

import com.virinchi.demo.model.signupModel;
import com.virinchi.demo.repository.signupRepo;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class loginController {

    @Autowired private signupRepo signupRepo;

    @PostMapping("/login")
    public String loginPage(@ModelAttribute signupModel form, Model m, HttpSession session) {
        String username = form.getUsername() == null ? "" : form.getUsername().trim();
        String rawPassword = form.getPassword() == null ? "" : form.getPassword();
        String hashPassword = DigestUtils.md5Hex(rawPassword.getBytes());

        boolean ok = signupRepo.existsByUsernameAndPassword(username, hashPassword);

        if (ok) {
            session.setAttribute("activeUser", username);
            session.setMaxInactiveInterval(20 * 60); // 20 minutes
            return "redirect:/homepage";
        } else {
            m.addAttribute("loginerror", "Username or Password Incorrect");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?loggedout";
    }
}
