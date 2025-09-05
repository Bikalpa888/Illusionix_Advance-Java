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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getLogoDarkUrl() { return logoDarkUrl; }
    public void setLogoDarkUrl(String logoDarkUrl) { this.logoDarkUrl = logoDarkUrl; }
}

