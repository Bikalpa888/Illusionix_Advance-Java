package com.virinchi.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homepageController {

    @GetMapping("/")
    private String homepage() {
        return "homepage";
    }


    @GetMapping("/user_dashboard")
    private String dashboard() {
        return "user_dashboard";
    }


    @GetMapping("/homepage")
    private String home() {
        return "homepage";
    }

    @GetMapping("/aboutus" )
    private String aboutusPage() {
        return "aboutus";
    }
    @GetMapping("/admin_dashboard" )
    private String adminDashboard() {
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
    private String productCategories() {
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
