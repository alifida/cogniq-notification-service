package com.cognitivequantum.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single notification log entry for dashboard inbox")
public class NotificationLogDto {

	private String id;
	private String templateCode;
	private String channel;
	private String status;
	private String subject;
	private String bodyPreview;
	private LocalDateTime sentAt;
	private LocalDateTime createdAt;
	private LocalDateTime readAt;

	@Schema(description = "True if notification has not been read")
	private boolean unread;
}
