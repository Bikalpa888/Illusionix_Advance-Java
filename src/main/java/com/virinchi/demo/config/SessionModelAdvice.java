package com.virinchi.demo.config;

import com.virinchi.demo.repository.SiteSettingsRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice(annotations = Controller.class)
public class SessionModelAdvice {

    private final ObjectProvider<SiteSettingsRepository> siteSettingsProvider;

    public SessionModelAdvice(ObjectProvider<SiteSettingsRepository> siteSettingsProvider) {
        this.siteSettingsProvider = siteSettingsProvider;
    }

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

        // Branding
        String logoUrl = "/images/logo.png"; // default bundled asset
        String logoDarkUrl = logoUrl;
        try {
            SiteSettingsRepository repo = siteSettingsProvider.getIfAvailable();
            if (repo != null) {
                var all = repo.findAll();
                if (!all.isEmpty()) {
                    var s = all.get(0);
                    if (s.getLogoUrl() != null && !s.getLogoUrl().isBlank()) logoUrl = s.getLogoUrl();
                    if (s.getLogoDarkUrl() != null && !s.getLogoDarkUrl().isBlank()) logoDarkUrl = s.getLogoDarkUrl();
                    if (s.getStoreName() != null) model.addAttribute("storeName", s.getStoreName());
                    if (s.getSupportEmail() != null) model.addAttribute("supportEmail", s.getSupportEmail());
                    if (s.getCurrency() != null) model.addAttribute("storeCurrency", s.getCurrency());
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("brandLogoUrl", logoUrl);
        model.addAttribute("brandLogoDarkUrl", logoDarkUrl);
    }
}
