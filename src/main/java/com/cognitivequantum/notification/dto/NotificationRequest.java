package com.cognitivequantum.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Schema(description = "Request to send a notification via one or more channels")
public class NotificationRequest {

	@NotNull
	@Schema(description = "Organization ID for isolation and history", requiredMode = Schema.RequiredMode.REQUIRED)
	private UUID orgId;

	@NotNull
	@Schema(description = "Recipient user ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private UUID recipientId;

	@Schema(description = "Recipient email (required for EMAIL channel; callers may pass from auth context)")
	private String recipientEmail;

	@NotBlank
	@Schema(description = "Template code (e.g. BILLING_SUCCESS, VERIFICATION_CODE)", requiredMode = Schema.RequiredMode.REQUIRED, example = "BILLING_SUCCESS")
	private String templateId;

	@NotEmpty
	@Schema(description = "Channels to use: EMAIL, WEB_SOCKET", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<String> channels;

	@Schema(description = "Template parameters for placeholder replacement (e.g. amount, credits, name)")
	private Map<String, Object> params;
}
