package com.dental.clinic.management.chatbot.service;

import com.dental.clinic.management.chatbot.domain.ChatbotKnowledge;
import com.dental.clinic.management.chatbot.repository.ChatbotKnowledgeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotKnowledgeRepository knowledgeRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chatbot.gemini.api-key}")
    private String apiKey;

    @Value("${chatbot.gemini.model-name:gemini-2.0-flash}")
    private String modelName;

    private String geminiApiUrl;

    @PostConstruct
    public void init() {
        this.geminiApiUrl = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, apiKey);
        log.info("Chatbot Gemini AI initialized with model: {} (REST API v1beta)", modelName);
    }

    public String chat(String userMessage) {
        List<ChatbotKnowledge> knowledgeBase = knowledgeRepository.findByIsActiveTrue();

        String listIds = knowledgeBase.stream()
                .map(ChatbotKnowledge::getKnowledgeId)
                .collect(Collectors.joining(", "));

        String prompt = "Task: Classify user message into EXACTLY ONE ID from this list: [" + listIds + "].\n\n" +
                "User message: \"" + userMessage + "\"\n\n" +
                "Rules:\n" +
                "1. Return ONLY the complete ID from the list (e.g., 'SYMPTOM_TOOTHACHE', NOT 'SYMPTOM')\n" +
                "2. Do NOT abbreviate or modify the ID\n" +
                "3. If no match found, return 'UNKNOWN'\n" +
                "4. Return ONLY the ID, no explanation\n\n" +
                "Your answer:";

        String detectedId;
        try {
            detectedId = callGeminiApi(prompt).trim();
            log.info("User message: '{}' -> Detected ID: '{}'", userMessage, detectedId);
        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage(), e);
            return "Dạ em đang gặp sự cố kỹ thuật. Anh/Chị vui lòng gọi Hotline 0909.123.456 để được hỗ trợ ạ!";
        }

        Optional<ChatbotKnowledge> match = knowledgeBase.stream()
                .filter(k -> k.getKnowledgeId().equals(detectedId))
                .findFirst();

        if (match.isPresent()) {
            return match.get().getResponse();
        } else {
            return "Dạ em chưa hiểu rõ ý mình lắm. Anh/Chị vui lòng gọi Hotline 076.400.9726 để được hỗ trợ ạ!";
        }
    }

    private String callGeminiApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.0,
                        "maxOutputTokens", 100));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = restTemplate.exchange(
                geminiApiUrl,
                HttpMethod.POST,
                request,
                Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        }

        throw new RuntimeException("Phản hồi Gemini API không hợp lệ");
    }
}
