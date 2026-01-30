package com.cognitivequantum.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of sending a notification")
public class NotificationResponse {

	@Schema(description = "Whether at least one channel succeeded")
	private boolean success;

	@Schema(description = "Per-channel status: channel -> success/fail message")
	private Map<String, String> channelResults;
}
