package com.virinchi.demo.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice(annotations = Controller.class)
public class SessionModelAdvice {

    @ModelAttribute
    public void addGlobalUserAttributes(Model model, HttpSession session) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        Object active = session.getAttribute("activeUser");
        boolean loggedIn = active != null || isAdmin;

        String name = null;
        String email = null;

        Object sName = session.getAttribute("userName");
        Object sEmail = session.getAttribute("userEmail");
        Object aEmail = session.getAttribute("adminEmail");

        if (isAdmin) {
            name = (sName instanceof String) ? (String) sName : "ADMIN";
            email = (aEmail instanceof String) ? (String) aEmail : null;
        } else {
            name = (sName instanceof String) ? (String) sName : (active instanceof String ? (String) active : null);
            email = (sEmail instanceof String) ? (String) sEmail : null;
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUserName", name);
        model.addAttribute("currentUserEmail", email);
    }
}

