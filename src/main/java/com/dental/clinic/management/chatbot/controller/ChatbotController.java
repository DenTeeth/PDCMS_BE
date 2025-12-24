package com.dental.clinic.management.chatbot.controller;

import com.dental.clinic.management.chatbot.dto.ChatRequest;
import com.dental.clinic.management.chatbot.dto.ChatResponse;
import com.dental.clinic.management.chatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * API Chatbot FAQ sử dụng Gemini AI
 * Public endpoint - không cần authentication
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "Chatbot FAQ với Gemini AI")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    @Operation(summary = "Gửi tin nhắn đến chatbot", description = "Chatbot sử dụng Gemini AI để trả lời câu hỏi FAQ")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat message: {}", request.getMessage());

        String response = chatbotService.chat(request.getMessage());

        ChatResponse chatResponse = ChatResponse.builder()
                .message(response)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        return ResponseEntity.ok(chatResponse);
    }
}
