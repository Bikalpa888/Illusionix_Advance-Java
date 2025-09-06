package com.virinchi.demo.controller;

import com.virinchi.demo.model.SiteSettings;
import com.virinchi.demo.model.signupModel;
import com.virinchi.demo.repository.SiteSettingsRepository;
import com.virinchi.demo.repository.signupRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AdminSettingsController {
    private final SiteSettingsRepository siteSettingsRepository;
    private final signupRepo signupRepo;
    private final JavaMailSender mailSender;

    public AdminSettingsController(SiteSettingsRepository siteSettingsRepository,
                                   signupRepo signupRepo,
                                   JavaMailSender mailSender) {
        this.siteSettingsRepository = siteSettingsRepository;
        this.signupRepo = signupRepo;
        this.mailSender = mailSender;
    }

    private boolean isAdmin(HttpSession session){
        Object a = session.getAttribute("isAdmin");
        return a instanceof Boolean && (Boolean) a;
    }

    @PostMapping("/admin/settings")
    public String saveSettings(@RequestParam(value = "storeName", required = false) String storeName,
                               @RequestParam(value = "supportEmail", required = false) String supportEmail,
                               @RequestParam(value = "currency", required = false) String currency,
                               @RequestParam(value = "logoUrl", required = false) String logoUrl,
                               @RequestParam(value = "logoDarkUrl", required = false) String logoDarkUrl,
                               HttpSession session, Model model){
        if(!isAdmin(session)) return "redirect:/login?admin=required";
        SiteSettings s = siteSettingsRepository.findAll().stream().findFirst().orElse(new SiteSettings());
        if(storeName != null) s.setStoreName(storeName);
        if(supportEmail != null) s.setSupportEmail(supportEmail);
        if(currency != null) s.setCurrency(currency);
        if(logoUrl != null) s.setLogoUrl(logoUrl);
        if(logoDarkUrl != null) s.setLogoDarkUrl(logoDarkUrl);
        siteSettingsRepository.save(s);
        return "redirect:/admin_dashboard?settings_saved";
    }

    @PostMapping("/admin/email/send")
    public String sendEmail(@RequestParam(value = "toEmail", required = false) String toEmail,
                            @RequestParam(value = "userId", required = false) Integer userId,
                            @RequestParam("subject") String subject,
                            @RequestParam("message") String message,
                            HttpSession session){
        if(!isAdmin(session)) return "redirect:/login?admin=required";
        String recipient = toEmail;
        if((recipient == null || recipient.isBlank()) && userId != null){
            signupModel u = null; try { u = signupRepo.findById(userId).orElse(null);} catch (Exception ignored) {}
            if(u != null) recipient = u.getEmail();
        }
        if(recipient == null || recipient.isBlank()){
            // fallback to store/support email from settings
            try {
                var s = siteSettingsRepository.findAll();
                if(!s.isEmpty() && s.get(0).getSupportEmail()!=null) recipient = s.get(0).getSupportEmail();
            } catch (Exception ignored) {}
        }
        if(recipient == null || recipient.isBlank()) return "redirect:/admin_dashboard?email=missing_recipient";
        try {
            if(mailSender != null){
                SimpleMailMessage sm = new SimpleMailMessage();
                sm.setTo(recipient);
                sm.setSubject(subject==null?"":subject);
                sm.setText(message==null?"":message);
                mailSender.send(sm);
            }
            return "redirect:/admin_dashboard?email=sent";
        } catch (Exception e){
            return "redirect:/admin_dashboard?email=send_failed";
        }
    }
}
