package com.dental.clinic.management.chatbot.service;

import com.dental.clinic.management.chatbot.domain.ChatbotKnowledge;
import com.dental.clinic.management.chatbot.repository.ChatbotKnowledgeRepository;
import com.dental.clinic.management.service.domain.DentalService;
import com.dental.clinic.management.service.repository.DentalServiceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotKnowledgeRepository knowledgeRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chatbot.gemini.api-key}")
    private String apiKey;

    @Value("${chatbot.gemini.model-name:gemini-2.0-flash}")
    private String modelName;

    private String geminiApiUrl;

    // Vietnamese currency formatter
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    @PostConstruct
    public void init() {
        this.geminiApiUrl = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, apiKey);
        log.info("Chatbot Gemini AI initialized with model: {} (REST API v1beta)", modelName);
    }

    public String chat(String userMessage) {
        List<ChatbotKnowledge> knowledgeBase = knowledgeRepository.findByIsActiveTrue();

        // Add dynamic IDs for database queries
        String listIds = knowledgeBase.stream()
                .map(ChatbotKnowledge::getKnowledgeId)
                .collect(Collectors.joining(", "));
        listIds += ", PRICE_LIST, SERVICE_INFO, SERVICE_SEARCH";

        String prompt = buildClassificationPrompt(userMessage, listIds);

        String detectedId;
        try {
            detectedId = callGeminiApi(prompt).trim().toUpperCase();
            // Clean up response - remove quotes, asterisks, extra whitespace
            detectedId = detectedId.replaceAll("[\"*\\s]", "");
            log.info("User message: '{}' -> Detected ID: '{}'", userMessage, detectedId);
        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage(), e);
            return "Dạ em đang gặp sự cố kỹ thuật. Anh/Chị vui lòng gọi Hotline 0909.123.456 để được hỗ trợ ạ!";
        }

        // Handle dynamic database queries
        if ("PRICE_LIST".equals(detectedId)) {
            return buildPriceListResponse();
        }
        if ("SERVICE_INFO".equals(detectedId) || "SERVICE_SEARCH".equals(detectedId)) {
            return handleServiceQuery(userMessage);
        }

        // Check static knowledge base
        Optional<ChatbotKnowledge> match = knowledgeBase.stream()
                .filter(k -> k.getKnowledgeId().equalsIgnoreCase(detectedId))
                .findFirst();

        if (match.isPresent()) {
            return match.get().getResponse();
        } else {
            // Try intelligent response using Gemini
            return generateIntelligentResponse(userMessage);
        }
    }

    /**
     * Build prompt for classification
     */
    private String buildClassificationPrompt(String userMessage, String listIds) {
        return """
                Task: Classify user message into EXACTLY ONE ID from this list: [%s].

                Classification rules:
                - GREETING: chào hỏi, xin chào, hello, hi
                - PRICE_LIST: hỏi về bảng giá, giá dịch vụ, bao nhiêu tiền (general)
                - SERVICE_INFO: hỏi về dịch vụ cụ thể (tên dịch vụ, chi tiết)
                - SERVICE_SEARCH: tìm kiếm dịch vụ theo keyword
                - SYMPTOM_TOOTHACHE: đau răng, nhức răng
                - ADDRESS: địa chỉ, ở đâu, location
                - UNKNOWN: không thuộc các loại trên

                User message: "%s"

                Rules:
                1. Return ONLY the ID, no explanation
                2. If asking about price/cost, return PRICE_LIST
                3. If asking about specific service by name, return SERVICE_INFO

                Your answer:
                """.formatted(listIds, userMessage);
    }

    /**
     * Build price list response from database
     */
    private String buildPriceListResponse() {
        try {
            List<DentalService> services = dentalServiceRepository.findAllActiveServicesWithCategory();

            if (services.isEmpty()) {
                return "Dạ hiện tại chưa có thông tin bảng giá. Anh/Chị vui lòng liên hệ Hotline 076.400.9726 để được tư vấn ạ!";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📋 **BẢNG GIÁ DỊCH VỤ NHA KHOA**\n\n");

            String currentCategory = "";
            for (DentalService service : services) {
                String categoryName = service.getCategory() != null
                        ? service.getCategory().getCategoryName()
                        : "Dịch vụ khác";

                if (!categoryName.equals(currentCategory)) {
                    currentCategory = categoryName;
                    sb.append("\n**").append(categoryName).append("**\n");
                }

                sb.append("• ").append(service.getServiceName())
                        .append(": ").append(formatPrice(service.getPrice())).append("\n");
            }

            sb.append("\n💡 Giá có thể thay đổi tùy theo tình trạng cụ thể.");
            sb.append("\n📞 Liên hệ Hotline: 076.400.9726 để được tư vấn chi tiết!");

            return sb.toString();
        } catch (Exception e) {
            log.error("Error building price list: {}", e.getMessage(), e);
            return "Dạ em không thể lấy bảng giá lúc này. Anh/Chị vui lòng gọi Hotline 076.400.9726 để được hỗ trợ ạ!";
        }
    }

    /**
     * Handle service-specific queries
     */
    private String handleServiceQuery(String userMessage) {
        try {
            List<DentalService> services = dentalServiceRepository.findAllActiveServicesWithCategory();

            // Search for matching services
            String searchLower = userMessage.toLowerCase();
            List<DentalService> matched = services.stream()
                    .filter(s -> s.getServiceName().toLowerCase().contains(searchLower)
                            || (s.getDescription() != null && s.getDescription().toLowerCase().contains(searchLower))
                            || searchLower.contains(s.getServiceName().toLowerCase()))
                    .limit(5)
                    .toList();

            if (matched.isEmpty()) {
                return "Dạ em không tìm thấy dịch vụ phù hợp. Anh/Chị có thể hỏi \"bảng giá\" để xem danh sách dịch vụ hoặc gọi Hotline 076.400.9726 ạ!";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🔍 **Dịch vụ phù hợp:**\n\n");

            for (DentalService service : matched) {
                sb.append("**").append(service.getServiceName()).append("**\n");
                sb.append("• Giá: ").append(formatPrice(service.getPrice())).append("\n");
                if (service.getDefaultDurationMinutes() != null) {
                    sb.append("• Thời gian: ~").append(service.getDefaultDurationMinutes()).append(" phút\n");
                }
                if (service.getDescription() != null && !service.getDescription().isEmpty()) {
                    String desc = service.getDescription();
                    if (desc.length() > 100) {
                        desc = desc.substring(0, 100) + "...";
                    }
                    sb.append("• Mô tả: ").append(desc).append("\n");
                }
                sb.append("\n");
            }

            sb.append("📞 Liên hệ Hotline: 076.400.9726 để đặt lịch!");

            return sb.toString();
        } catch (Exception e) {
            log.error("Error handling service query: {}", e.getMessage(), e);
            return "Dạ em gặp lỗi khi tìm kiếm. Anh/Chị vui lòng thử lại hoặc gọi Hotline 076.400.9726 ạ!";
        }
    }

    /**
     * Generate intelligent response using Gemini for unmatched queries
     */
    private String generateIntelligentResponse(String userMessage) {
        try {
            // Get service list for context
            List<DentalService> services = dentalServiceRepository.findAllActiveServicesWithCategory();
            String serviceContext = services.stream()
                    .limit(10)
                    .map(s -> s.getServiceName() + " (" + formatPrice(s.getPrice()) + ")")
                    .collect(Collectors.joining(", "));

            String prompt = """
                    Bạn là trợ lý ảo của phòng khám nha khoa DenTeeth.

                    Thông tin phòng khám:
                    - Địa chỉ: Lô E2a-7, Đường D1, Khu Công nghệ cao, P. Long Thạnh Mỹ, TP. Thủ Đức, TPHCM
                    - Hotline: 076.400.9726
                    - Một số dịch vụ: %s

                    Câu hỏi của khách: "%s"

                    Trả lời ngắn gọn, thân thiện (dưới 100 từ). Nếu không biết, đề nghị gọi hotline.
                    """.formatted(serviceContext, userMessage);

            String response = callGeminiApi(prompt);
            return response.trim();
        } catch (Exception e) {
            log.error("Error generating intelligent response: {}", e.getMessage(), e);
            return "Dạ em chưa hiểu rõ ý Anh/Chị lắm. Vui lòng gọi Hotline 076.400.9726 để được hỗ trợ trực tiếp ạ!";
        }
    }

    /**
     * Format price to Vietnamese currency
     */
    private String formatPrice(java.math.BigDecimal price) {
        if (price == null)
            return "Liên hệ";
        return VND_FORMAT.format(price) + " VNĐ";
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
