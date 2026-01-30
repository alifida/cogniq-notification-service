package com.cognitivequantum.notification.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Extracts user identity for inbox and WebSocket. Supports:
 * 1) X-User-Id header (set by Gateway after JWT validation) – preferred.
 * 2) Authorization: Bearer &lt;JWT&gt; – validates JWT and uses userId, org_id claims.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String HEADER_ORG_ID = "X-Org-Id";
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	@Value("${cogniq.jwt.secret:}")
	private String jwtSecret;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws ServletException, IOException {
		String path = request.getRequestURI();
		try {
			if (path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs") || path.startsWith("/ws")) {
				filterChain.doFilter(request, response);
				return;
			}
			// Internal API: no JWT required (service-to-service)
			if (path.startsWith("/internal")) {
				filterChain.doFilter(request, response);
				return;
			}
			UUID userId = null;
			UUID orgId = null;
			List<SimpleGrantedAuthority> roles = Collections.emptyList();

			String hUserId = request.getHeader(HEADER_USER_ID);
			String hOrgId = request.getHeader(HEADER_ORG_ID);
			if (hUserId != null && !hUserId.isBlank()) {
				try {
					userId = UUID.fromString(hUserId.trim());
					if (hOrgId != null && !hOrgId.isBlank()) orgId = UUID.fromString(hOrgId.trim());
				} catch (IllegalArgumentException e) {
					log.warn("Invalid tenant headers");
				}
			}
			if (userId == null && jwtSecret != null && !jwtSecret.isBlank()) {
				Key key = getSignInKey();
				if (key != null) {
					String authHeader = request.getHeader(HEADER_AUTHORIZATION);
					if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
						String token = authHeader.substring(BEARER_PREFIX.length());
						try {
							Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
							Object userIdClaim = claims.get("userId");
							if (userIdClaim != null) userId = UUID.fromString(userIdClaim.toString());
							Object orgIdClaim = claims.get("org_id");
							if (orgIdClaim != null) orgId = UUID.fromString(orgIdClaim.toString());
							@SuppressWarnings("unchecked")
							List<String> roleList = claims.get("roles", List.class);
							if (roleList != null) {
								roles = roleList.stream()
									.map(r -> new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r))
									.collect(Collectors.toList());
							}
						} catch (Exception e) {
							log.debug("JWT validation failed: {}", e.getMessage());
						}
					}
				}
			}
			if (userId != null) {
				UserIdPrincipal principal = UserIdPrincipal.of(userId, orgId);
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
					principal, null, roles.isEmpty() ? List.of(new SimpleGrantedAuthority("ROLE_USER")) : roles);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			filterChain.doFilter(request, response);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	private Key getSignInKey() {
		if (jwtSecret == null || jwtSecret.isBlank()) return null;
		try {
			byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (Exception e) {
			return null;
		}
	}
}
