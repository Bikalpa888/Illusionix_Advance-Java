package com.virinchi.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import com.virinchi.demo.repository.ProductRepository;
import com.virinchi.demo.model.Product;
import com.virinchi.demo.repository.ContactMessageRepository;
import com.virinchi.demo.repository.NewsletterSubscriptionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homepageController {

    private final ProductRepository productRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private static final Logger log = LoggerFactory.getLogger(homepageController.class);

    public homepageController(ProductRepository productRepository,
                              ContactMessageRepository contactMessageRepository,
                              NewsletterSubscriptionRepository newsletterSubscriptionRepository) {
        this.productRepository = productRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
    }

    @GetMapping("/")
    private String homepage() {
        return "homepage";
    }


    @GetMapping("/user_dashboard")
    private String dashboard() {
        return "user_dashboard";
    }


    @GetMapping("/homepage")
    public String homepage(HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login?auth=required";
        }
        return "homepage";
    }

    @GetMapping("/aboutus" )
    private String aboutusPage() {
        return "aboutus";
    }
    @GetMapping("/admin_dashboard" )
    private String adminDashboard(HttpSession session, Model model) {
        Object isAdmin = session.getAttribute("isAdmin");
        if (!(isAdmin instanceof Boolean) || !((Boolean) isAdmin)) {
            return "redirect:/login?admin=required";
        }
        model.addAttribute("products", productRepository.findAll());
        try {
            model.addAttribute("messages", contactMessageRepository.findTop50ByOrderByCreatedAtDesc());
            model.addAttribute("subscribers", newsletterSubscriptionRepository.findTop50ByOrderByCreatedAtDesc());
        } catch (Exception ignored) {}
        return "admin_dashboard";
    }
    @GetMapping("/contact")
    private String contactPage() {
        return "contact";
    }
    @GetMapping("/dashboard_login" )
    private String dashboardLogin() {
        return "dashboard_login";
    }
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @GetMapping("/product_categories")
    public String productCategories(Model model) {
        var all = productRepository.findAll();
        try {
            log.info("/product_categories -> loaded {} products", all.size());
        } catch (Exception ignored) {}
        // Always include raw list as well for template compatibility
        model.addAttribute("products", all);
        var views = new java.util.ArrayList<java.util.Map<String,Object>>();
        var om = new ObjectMapper();
        for (Product p : all) {
            var m = new java.util.LinkedHashMap<String,Object>();
            m.put("sku", p.getSku());
            m.put("name", p.getName());
            m.put("description", p.getDescription());
            m.put("price", p.getPrice());
            // parse first image if JSON array present
            String first = null;
            String json = p.getImagesJson();
            if (json != null && !json.isBlank()) {
                try {
                    java.util.List<String> imgs = om.readValue(json, new TypeReference<java.util.List<String>>(){});
                    if (imgs != null && !imgs.isEmpty()) first = imgs.get(0);
                } catch (Exception ignored) {}
            }
            m.put("image", first);
            views.add(m);
        }
        model.addAttribute("productViews", views);
        return "product_categories";
    }
    @GetMapping("/product_detail")
    private String productDetail() {
        return "product_detail";
    }
    @GetMapping("/shopping_cart_checkout")
    private String checkoutPage() {
        return "shopping_cart_checkout";
    }
    @GetMapping("/signup" )
    private String signup() {
        return "signup";
    }
    @GetMapping("/virtual_showroom" )
    private String virtualTour() {
        return "virtual_showroom";
    }

    @GetMapping("/ar_tryon")
    private String arTryon() {
        return "ar_tryon";
    }

    @GetMapping("/vr_experience")
    private String vrExperience() {
        return "vr_experience";
    }

}
