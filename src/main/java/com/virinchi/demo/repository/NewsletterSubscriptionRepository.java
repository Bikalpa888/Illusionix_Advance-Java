package com.virinchi.demo.repository;

import com.virinchi.demo.model.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
    boolean existsByEmail(String email);
    java.util.List<NewsletterSubscription> findTop50ByOrderByCreatedAtDesc();
}

