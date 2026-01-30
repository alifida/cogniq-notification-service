package com.cognitivequantum.notification.channels.email;

import com.cognitivequantum.notification.entity.NotificationLogStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.UUID;

/**
 * Email delivery via JavaMailSender (SMTP / SendGrid / Amazon SES).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailChannel {

	private final JavaMailSender mailSender;

	@Value("${cogniq.notification.from-email:noreply@cogniq.local}")
	private String fromEmail;

	@Value("${cogniq.notification.from-name:CogniQ Flow}")
	private String fromName;

	/**
	 * Send HTML email. Falls back to plain text if htmlBody is null/blank.
	 */
	public SendResult send(UUID recipientId, String toEmail, String subject, String htmlBody, String plainBody) {
		if (toEmail == null || toEmail.isBlank()) {
			log.warn("No recipient email for user {}", recipientId);
			return new SendResult(NotificationLogStatus.SKIPPED, "No recipient email");
		}
		try {
			if (htmlBody != null && !htmlBody.isBlank()) {
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
				helper.setFrom(fromEmail, fromName);
				helper.setTo(toEmail);
				helper.setSubject(subject);
				helper.setText(plainBody != null ? plainBody : stripHtml(htmlBody), htmlBody);
				mailSender.send(message);
			} else {
				SimpleMailMessage msg = new SimpleMailMessage();
				msg.setFrom(fromEmail);
				msg.setTo(toEmail);
				msg.setSubject(subject);
				msg.setText(plainBody != null ? plainBody : "");
				mailSender.send(msg);
			}
			log.debug("Email sent to {} for user {}", toEmail, recipientId);
			return new SendResult(NotificationLogStatus.SENT_SUCCESS, null);
		} catch (MessagingException e) {
			log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
			return new SendResult(NotificationLogStatus.SENT_FAIL, e.getMessage());
		} catch (Exception e) {
			log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
			return new SendResult(NotificationLogStatus.SENT_FAIL, e.getMessage());
		}
	}

	private static String stripHtml(String html) {
		return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
	}

	public record SendResult(NotificationLogStatus status, String errorMessage) {}
}
