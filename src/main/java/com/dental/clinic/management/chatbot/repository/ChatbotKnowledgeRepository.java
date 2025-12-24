package com.dental.clinic.management.chatbot.repository;

import com.dental.clinic.management.chatbot.domain.ChatbotKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotKnowledgeRepository extends JpaRepository<ChatbotKnowledge, String> {

    List<ChatbotKnowledge> findByIsActiveTrue();
}
