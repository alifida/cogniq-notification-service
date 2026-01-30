package com.cognitivequantum.notification.controller;

import com.cognitivequantum.notification.dto.NotificationRequest;
import com.cognitivequantum.notification.dto.NotificationResponse;
import com.cognitivequantum.notification.service.DispatcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Internal", description = "Internal API for other microservices – not exposed via Gateway")
public class InternalNotificationController {

	private final DispatcherService dispatcherService;

	@PostMapping("/send")
	@Operation(summary = "Send notification", description = "Called by auth, billing, orchestrator, data services to send alerts")
	public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
		NotificationResponse response = dispatcherService.send(request);
		return ResponseEntity.ok(response);
	}
}
