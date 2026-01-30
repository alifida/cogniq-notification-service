package com.cognitivequantum.notification.channels.websocket;

import com.cognitivequantum.notification.entity.NotificationLogStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Real-time delivery via STOMP to /topic/user-{userId}/alerts.
 * Clients subscribe to their own topic; JWT secures the connection.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannel {

	private static final String DESTINATION_PREFIX = "/topic/user-";
	private static final String DESTINATION_SUFFIX = "/alerts";

	private final SimpMessagingTemplate messagingTemplate;

	/**
	 * Push a JSON payload to the user's alert topic. If no one is subscribed, message is dropped (fire-and-forget).
	 */
	public SendResult send(UUID recipientId, String templateCode, String subject, Map<String, Object> payload) {
		String destination = DESTINATION_PREFIX + recipientId + DESTINATION_SUFFIX;
		try {
			Map<String, Object> message = Map.of(
				"templateCode", templateCode != null ? templateCode : "",
				"subject", subject != null ? subject : "",
				"payload", payload != null ? payload : Map.of(),
				"timestamp", System.currentTimeMillis()
			);
			messagingTemplate.convertAndSend(destination, message);
			log.debug("WebSocket sent to {} for user {}", destination, recipientId);
			return new SendResult(NotificationLogStatus.SENT_SUCCESS, null);
		} catch (Exception e) {
			log.warn("Failed to send WebSocket to {}: {}", destination, e.getMessage());
			return new SendResult(NotificationLogStatus.SENT_FAIL, e.getMessage());
		}
	}

	public record SendResult(NotificationLogStatus status, String errorMessage) {}
}
