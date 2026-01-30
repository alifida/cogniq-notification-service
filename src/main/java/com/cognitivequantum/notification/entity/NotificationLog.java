package com.cognitivequantum.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_log", indexes = {
	@Index(name = "idx_notification_log_org_id", columnList = "org_id"),
	@Index(name = "idx_notification_log_recipient_id", columnList = "recipient_id"),
	@Index(name = "idx_notification_log_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "org_id", nullable = false)
	private UUID orgId;

	@Column(name = "recipient_id", nullable = false)
	private UUID recipientId;

	@Column(name = "template_code", nullable = false, length = 64)
	private String templateCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false, length = 32)
	private NotificationChannel channel;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private NotificationLogStatus status;

	@Column(name = "subject", length = 512)
	private String subject;

	@Column(name = "body_preview", length = 1024)
	private String bodyPreview;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "error_message", length = 2048)
	private String errorMessage;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
