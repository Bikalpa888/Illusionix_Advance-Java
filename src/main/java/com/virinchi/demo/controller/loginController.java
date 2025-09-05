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

    // Dedicated admin credentials
    private static final String ADMIN_EMAIL = "admin@illusionix.com"; // change if needed
    private static final String ADMIN_PASSWORD = "user@123"; // change if needed

    @PostMapping("/login")
    public String loginPage(@ModelAttribute signupModel form, Model m, HttpSession session) {
        String usernameOrEmail = form.getUsername() == null ? "" : form.getUsername().trim();
        String rawPassword = form.getPassword() == null ? "" : form.getPassword();

        // 1) Admin login (strict match to configured credentials)
        if (!usernameOrEmail.isEmpty() && usernameOrEmail.equalsIgnoreCase(ADMIN_EMAIL)) {
            if (ADMIN_PASSWORD.equals(rawPassword)) {
                session.setAttribute("isAdmin", true);
                session.setAttribute("adminEmail", ADMIN_EMAIL);
                session.setAttribute("activeUser", "ADMIN");
                session.setMaxInactiveInterval(30 * 60); // 30 minutes for admin
                return "redirect:/admin_dashboard";
            } else {
                m.addAttribute("loginerror", "Invalid admin credentials");
                return "login";
            }
        }

        // 2) Regular user login (DB-backed)
        String hashPassword = DigestUtils.md5Hex(rawPassword.getBytes());
        boolean ok = signupRepo.existsByUsernameAndPassword(usernameOrEmail, hashPassword);

        if (ok) {
            session.setAttribute("activeUser", usernameOrEmail);
            session.setMaxInactiveInterval(20 * 60); // 20 minutes
            return "redirect:/homepage";
        }

        m.addAttribute("loginerror", "Username or Password Incorrect");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?loggedout";
    }
}
