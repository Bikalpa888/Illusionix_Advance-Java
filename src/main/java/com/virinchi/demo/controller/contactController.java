package com.virinchi.demo.controller;

import com.virinchi.demo.model.ContactMessage;
import com.virinchi.demo.model.ContactMessageForm;
import com.virinchi.demo.repository.ContactMessageRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class contactController {

    private final ContactMessageRepository contactRepo;
    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String notifyTo;

    public contactController(ContactMessageRepository contactRepo,
                             JavaMailSender mailSender,
                             @Value("${spring.mail.username:}") String mailFrom,
                             @Value("${app.contact.notifyTo:}") String notifyTo) {
        this.contactRepo = contactRepo;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.notifyTo = (notifyTo == null || notifyTo.isBlank()) ? mailFrom : notifyTo;
    }

    @PostMapping("/contact")
    public String submitContact(@Valid ContactMessageForm form, BindingResult result, HttpSession session) {
        if (result.hasErrors() || !form.isPrivacy()) {
            return "redirect:/contact?invalid=1";
        }
        ContactMessage cm = new ContactMessage();
        cm.setFirstName(form.getFirstName());
        cm.setLastName(form.getLastName());
        cm.setEmail(form.getEmail());
        cm.setPhone(form.getPhone());
        cm.setSubject(form.getSubject());
        cm.setMessage(form.getMessage());
        cm.setPrivacyAccepted(form.isPrivacy());
        contactRepo.save(cm);

        // Best-effort notification email
        try {
            if (mailSender != null && notifyTo != null && !notifyTo.isBlank()) {
                SimpleMailMessage msg = new SimpleMailMessage();
                if (mailFrom != null && !mailFrom.isBlank()) msg.setFrom(mailFrom);
                msg.setTo(notifyTo);
                msg.setSubject("New Contact Message: " + cm.getSubject());
                msg.setText("From: " + cm.getFirstName() + " " + cm.getLastName() + " <" + cm.getEmail() + ">\n" +
                        "Phone: " + (cm.getPhone() == null ? "-" : cm.getPhone()) + "\n\n" + cm.getMessage());
                mailSender.send(msg);
            }
        } catch (Exception ignored) {}

        return "redirect:/contact?sent=1";
    }
}
