package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.NotificationTranslations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTranslationRepository extends JpaRepository<NotificationTranslations, Long> {
}
