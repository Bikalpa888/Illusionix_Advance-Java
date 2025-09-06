package com.virinchi.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "site_settings")
public class SiteSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logo_url")
    private String logoUrl; // e.g., /images/logo.png or CDN url

    @Column(name = "logo_dark_url")
    private String logoDarkUrl; // optional dark-mode logo

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "support_email")
    private String supportEmail;

    @Column(name = "currency")
    private String currency;

    @Column(name = "notify_to_email")
    private String notifyToEmail; // default notification recipient for admin emails

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getLogoDarkUrl() { return logoDarkUrl; }
    public void setLogoDarkUrl(String logoDarkUrl) { this.logoDarkUrl = logoDarkUrl; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getSupportEmail() { return supportEmail; }
    public void setSupportEmail(String supportEmail) { this.supportEmail = supportEmail; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getNotifyToEmail() { return notifyToEmail; }
    public void setNotifyToEmail(String notifyToEmail) { this.notifyToEmail = notifyToEmail; }
}
