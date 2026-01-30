package com.cognitivequantum.notification.config;

import java.util.UUID;

/**
 * Principal holding current user and tenant context (org_id).
 */
public record UserIdPrincipal(UUID userId, UUID orgId) {

	public static UserIdPrincipal of(UUID userId, UUID orgId) {
		return new UserIdPrincipal(userId, orgId);
	}
}
