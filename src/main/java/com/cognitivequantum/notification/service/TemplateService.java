package com.cognitivequantum.notification.service;

import com.cognitivequantum.notification.entity.NotificationTemplate;
import com.cognitivequantum.notification.exception.TemplateNotFoundException;
import com.cognitivequantum.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

	private final NotificationTemplateRepository templateRepository;

	public NotificationTemplate getByCode(String code) {
		return templateRepository.findByCode(code)
			.orElseThrow(() -> new TemplateNotFoundException("Template not found: " + code));
	}

	/**
	 * Replace placeholders (e.g. {{name}}, {{amount}}) in subject and body.
	 */
	public String renderSubject(String templateCode, Map<String, Object> params) {
		NotificationTemplate t = getByCode(templateCode);
		return replacePlaceholders(t.getSubject(), params);
	}

	public String renderBodyHtml(String templateCode, Map<String, Object> params) {
		NotificationTemplate t = getByCode(templateCode);
		String body = t.getBodyHtml() != null ? t.getBodyHtml() : t.getBodyPlain();
		return replacePlaceholders(body != null ? body : "", params);
	}

	public String renderBodyPlain(String templateCode, Map<String, Object> params) {
		NotificationTemplate t = getByCode(templateCode);
		String body = t.getBodyPlain() != null ? t.getBodyPlain() : t.getBodyHtml();
		if (body == null) return "";
		return replacePlaceholders(stripHtml(body), params);
	}

	public static String replacePlaceholders(String content, Map<String, Object> params) {
		if (content == null) return "";
		if (params == null || params.isEmpty()) return content;
		String out = content;
		for (Map.Entry<String, Object> e : params.entrySet()) {
			String placeholder = "{{" + e.getKey() + "}}";
			String value = e.getValue() != null ? e.getValue().toString() : "";
			out = out.replace(placeholder, value);
		}
		return out;
	}

	private static String stripHtml(String html) {
		if (html == null) return "";
		return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
	}
}
