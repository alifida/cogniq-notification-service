package com.cognitivequantum.notification.service;

import com.cognitivequantum.notification.channels.email.EmailChannel;
import com.cognitivequantum.notification.channels.websocket.WebSocketChannel;
import com.cognitivequantum.notification.dto.NotificationRequest;
import com.cognitivequantum.notification.dto.NotificationResponse;
import com.cognitivequantum.notification.entity.NotificationChannel;
import com.cognitivequantum.notification.entity.NotificationLog;
import com.cognitivequantum.notification.entity.NotificationLogStatus;
import com.cognitivequantum.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispatcherService {

	private final TemplateService templateService;
	private final EmailChannel emailChannel;
	private final WebSocketChannel webSocketChannel;
	private final NotificationLogRepository logRepository;

	/**
	 * Resolve template, render content, dispatch to requested channels, and log results.
	 */
	@Transactional
	public NotificationResponse send(NotificationRequest request) {
		Map<String, Object> params = request.getParams() != null ? request.getParams() : new HashMap<>();
		// Normalize string keys and values for template replacement
		Map<String, Object> stringParams = params.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? e.getValue().toString() : ""));

		String subject = templateService.renderSubject(request.getTemplateId(), stringParams);
		String bodyHtml = templateService.renderBodyHtml(request.getTemplateId(), stringParams);
		String bodyPlain = templateService.renderBodyPlain(request.getTemplateId(), stringParams);
		String bodyPreview = bodyPlain.length() > 500 ? bodyPlain.substring(0, 500) + "..." : bodyPlain;

		Map<String, String> channelResults = new HashMap<>();
		boolean anySuccess = false;

		for (String ch : request.getChannels()) {
			String channelUpper = ch != null ? ch.trim().toUpperCase() : "";
			if ("EMAIL".equals(channelUpper)) {
				EmailChannel.SendResult result = emailChannel.send(
					request.getRecipientId(),
					request.getRecipientEmail(),
					subject,
					bodyHtml,
					bodyPlain
				);
				saveLog(request, NotificationChannel.EMAIL, result, subject, bodyPreview);
				channelResults.put("EMAIL", result.status().name() + (result.errorMessage() != null ? ": " + result.errorMessage() : ""));
				if (result.status() == NotificationLogStatus.SENT_SUCCESS) anySuccess = true;
			} else if ("WEB_SOCKET".equals(channelUpper) || "WEBSOCKET".equals(channelUpper)) {
				WebSocketChannel.SendResult result = webSocketChannel.send(
					request.getRecipientId(),
					request.getTemplateId(),
					subject,
					params
				);
				saveLog(request, NotificationChannel.WEB_SOCKET, result, subject, bodyPreview);
				channelResults.put("WEB_SOCKET", result.status().name() + (result.errorMessage() != null ? ": " + result.errorMessage() : ""));
				if (result.status() == NotificationLogStatus.SENT_SUCCESS) anySuccess = true;
			}
		}

		log.info("Notification sent for template {} recipient {} channels {} success={}", request.getTemplateId(), request.getRecipientId(), request.getChannels(), anySuccess);
		return NotificationResponse.builder()
			.success(anySuccess)
			.channelResults(channelResults)
			.build();
	}

	private void saveLog(NotificationRequest request, NotificationChannel channel, Object result, String subject, String bodyPreview) {
		NotificationLogStatus status;
		String errorMessage = null;
		if (result instanceof EmailChannel.SendResult r) {
			status = r.status();
			errorMessage = r.errorMessage();
		} else if (result instanceof WebSocketChannel.SendResult r) {
			status = r.status();
			errorMessage = r.errorMessage();
		} else {
			status = NotificationLogStatus.SENT_FAIL;
		}
		NotificationLog log = NotificationLog.builder()
			.orgId(request.getOrgId())
			.recipientId(request.getRecipientId())
			.templateCode(request.getTemplateId())
			.channel(channel)
			.status(status)
			.subject(subject)
			.bodyPreview(bodyPreview.length() > 1024 ? bodyPreview.substring(0, 1024) : bodyPreview)
			.sentAt(status == NotificationLogStatus.SENT_SUCCESS ? LocalDateTime.now() : null)
			.errorMessage(errorMessage != null && errorMessage.length() > 2048 ? errorMessage.substring(0, 2048) : errorMessage)
			.build();
		logRepository.save(log);
	}
}
