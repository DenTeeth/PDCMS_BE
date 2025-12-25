package com.dental.clinic.management.chatbot.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity luu tru kien thuc co ban cho chatbot (FAQ)
 * Gemini AI se phan loai cau hoi vao ID phu hop
 */
@Entity
@Table(name = "chatbot_knowledge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotKnowledge {

    @Id
    @Column(name = "knowledge_id", length = 50)
    private String knowledgeId;

    @Column(name = "keywords", columnDefinition = "TEXT", nullable = false)
    private String keywords; // JSON array: ["xin chao", "hi", "hello"]

    @Column(name = "response", columnDefinition = "TEXT", nullable = false)
    private String response;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
