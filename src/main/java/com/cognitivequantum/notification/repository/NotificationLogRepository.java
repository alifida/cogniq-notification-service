package com.cognitivequantum.notification.repository;

import com.cognitivequantum.notification.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

	Page<NotificationLog> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

	Page<NotificationLog> findByRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

	long countByRecipientIdAndReadAtIsNull(UUID recipientId);

	Page<NotificationLog> findByOrgIdOrderByCreatedAtDesc(UUID orgId, Pageable pageable);
}
