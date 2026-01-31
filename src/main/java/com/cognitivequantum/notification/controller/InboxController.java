package com.cognitivequantum.notification.controller;

import com.cognitivequantum.notification.config.UserIdPrincipal;
import com.cognitivequantum.notification.dto.NotificationLogDto;
import com.cognitivequantum.notification.entity.NotificationLog;
import com.cognitivequantum.notification.repository.NotificationLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Inbox", description = "Dashboard notification history – requires JWT")
public class InboxController {

	private final NotificationLogRepository logRepository;

	@GetMapping("/inbox")
	@Operation(summary = "List my notifications", description = "Paginated list; unreadOnly=true for dropdown preview", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Map<String, Object>> listMyNotifications(
		@AuthenticationPrincipal UserIdPrincipal principal,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) Boolean unreadOnly
	) {
		if (principal == null) {
			return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
		}
		Pageable pageable = PageRequest.of(page, Math.min(size, 100));
		Page<NotificationLog> logPage = Boolean.TRUE.equals(unreadOnly)
			? logRepository.findByRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(principal.userId(), pageable)
			: logRepository.findByRecipientIdOrderByCreatedAtDesc(principal.userId(), pageable);
		Page<NotificationLogDto> dtoPage = logPage.map(this::toDto);
		return ResponseEntity.ok(Map.of(
			"content", dtoPage.getContent(),
			"totalElements", dtoPage.getTotalElements(),
			"totalPages", dtoPage.getTotalPages(),
			"number", dtoPage.getNumber(),
			"size", dtoPage.getSize()
		));
	}

	@GetMapping("/inbox/unread-count")
	@Operation(summary = "Unread count", description = "Badge count for header icon", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserIdPrincipal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		long count = logRepository.countByRecipientIdAndReadAtIsNull(principal.userId());
		return ResponseEntity.ok(Map.of("count", count));
	}

	@PatchMapping("/inbox/{id}/read")
	@Operation(summary = "Mark as read", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> markAsRead(
		@AuthenticationPrincipal UserIdPrincipal principal,
		@PathVariable UUID id
	) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		NotificationLog log = logRepository.findById(id).orElse(null);
		if (log == null || !log.getRecipientId().equals(principal.userId())) {
			return ResponseEntity.notFound().build();
		}
		log.setReadAt(LocalDateTime.now());
		logRepository.save(log);
		return ResponseEntity.noContent().build();
	}

	private NotificationLogDto toDto(NotificationLog log) {
		return NotificationLogDto.builder()
			.id(log.getId().toString())
			.templateCode(log.getTemplateCode())
			.channel(log.getChannel().name())
			.status(log.getStatus().name())
			.subject(log.getSubject())
			.bodyPreview(log.getBodyPreview())
			.sentAt(log.getSentAt())
			.createdAt(log.getCreatedAt())
			.readAt(log.getReadAt())
			.unread(log.getReadAt() == null)
			.build();
	}
}
