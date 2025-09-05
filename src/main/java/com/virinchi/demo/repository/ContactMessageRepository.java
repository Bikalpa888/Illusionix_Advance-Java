package com.virinchi.demo.repository;

import com.virinchi.demo.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    java.util.List<ContactMessage> findTop50ByOrderByCreatedAtDesc();
}
