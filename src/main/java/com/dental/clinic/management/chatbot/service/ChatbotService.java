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

    @Value("${chatbot.gemini.model-name:gemini-2.5-flash}")
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

        // Add dynamic IDs for database queries (single assignment for effectively
        // final)
        final String listIds = knowledgeBase.stream()
                .map(ChatbotKnowledge::getKnowledgeId)
                .collect(Collectors.joining(", "))
                + ", PRICE_LIST, SERVICE_INFO, SERVICE_SEARCH, OUT_OF_SCOPE";

        String prompt = buildClassificationPrompt(userMessage, listIds);

        final String detectedId;
        try {
            String rawResponse = callGeminiApi(prompt).trim().toUpperCase();
            // Clean up response - remove quotes, asterisks, extra whitespace
            detectedId = rawResponse.replaceAll("[\"*\\s]", "");
            log.info("User message: '{}' -> Detected ID: '{}'", userMessage, detectedId);
        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage(), e);
            return "Dạ em đang gặp sự cố kỹ thuật. Anh/Chị vui lòng gọi Hotline 076.400.9726 để được hỗ trợ ạ!";
        }

        // Handle dynamic database queries
        if ("OUT_OF_SCOPE".equals(detectedId)) {
            return getOutOfScopeResponse();
        }
        if ("PRICE_LIST".equals(detectedId)) {
            return buildPriceListResponse();
        }
        if ("SERVICE_INFO".equals(detectedId) || "SERVICE_SEARCH".equals(detectedId)) {
            return handleServiceQuery(userMessage);
        }
        // Handle symptom consultation (deterministic)
        String symptomResponse = getSymptomResponse(detectedId);
        if (symptomResponse != null) {
            return symptomResponse;
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
                - GREETING: chao hoi, xin chao, hello, hi
                - PRICE_LIST: hoi ve bang gia, gia dich vu, bao nhieu tien (general)
                - SERVICE_INFO: hoi ve dich vu cu the (ten dich vu, chi tiet)
                - SERVICE_SEARCH: tim kiem dich vu theo keyword
                - SYMPTOM_TOOTHACHE: dau rang, nhuc rang, rang dau
                - SYMPTOM_BLEEDING_GUMS: chay mau nuou, chay mau loi
                - SYMPTOM_LOOSE_TOOTH: rang lung lay, rang long
                - SYMPTOM_BAD_BREATH: hoi mieng, mieng hoi
                - SYMPTOM_SENSITIVE_TEETH: e buot, rang nhay cam
                - SYMPTOM_SWOLLEN_FACE: sung ma, sung mat
                - SYMPTOM_WISDOM_TOOTH: rang khon, rang so 8
                - ADDRESS: dia chi, o dau, location
                - OUT_OF_SCOPE: khong lien quan den nha khoa (game, thoi tiet, chinh tri, etc.)
                - UNKNOWN: khong thuoc cac loai tren

                User message: "%s"

                Rules:
                1. Return ONLY the ID, no explanation
                2. If asking about price/cost, return PRICE_LIST
                3. If asking about specific service by name, return SERVICE_INFO
                4. If message has NOTHING to do with dental/healthcare, return OUT_OF_SCOPE

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
            return "Lien he";
        return VND_FORMAT.format(price) + " VND";
    }

    /**
     * Response for out-of-scope questions (not dental related)
     */
    private String getOutOfScopeResponse() {
        return "Xin lỗi, em là trợ lý ảo chuyên về NHA KHOA của phòng khám DenTeeth.\n\n" +
                "Em có thể giúp bạn:\n" +
                "- Tra cứu bảng giá dịch vụ\n" +
                "- Tư vấn triệu chứng răng miệng\n" +
                "- Thông tin địa chỉ, giờ làm việc\n" +
                "- Hướng dẫn đặt lịch khám\n\n" +
                "Anh/Chị có câu hỏi gì về răng miệng không ạ?\n" +
                "Hotline: 076.400.9726";
    }

    /**
     * Deterministic symptom responses (same symptoms = same response ALWAYS)
     */
    private String getSymptomResponse(String symptomId) {
        return switch (symptomId) {
            case "SYMPTOM_TOOTHACHE" ->
                "[TRIỆU CHỨNG ĐAU RĂNG]\n\n" +
                        "Dựa trên triệu chứng, có thể là một trong các vấn đề sau:\n\n" +
                        "1. Sâu răng (Dental Caries) - Phổ biến nhất\n" +
                        "   - Đau khi ăn đồ ngọt, nóng, lạnh\n" +
                        "   - Có thể thấy lỗ đen trên răng\n\n" +
                        "2. Viêm tủy răng (Pulpitis)\n" +
                        "   - Đau dữ dội, kéo dài\n" +
                        "   - Đau tăng về đêm\n\n" +
                        "3. Áp xe răng (Dental Abscess)\n" +
                        "   - Sưng má, đau nhức liên tục\n" +
                        "   - Có thể sốt nhẹ\n\n" +
                        "Khuyến nghị: Nên khám bác sĩ sớm để xác định chính xác nguyên nhân.\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_BLEEDING_GUMS" ->
                "[TRIỆU CHỨNG CHẢY MÁU NƯỚU]\n\n" +
                        "Có thể là dấu hiệu của:\n\n" +
                        "1. Viêm nướu (Gingivitis) - Phổ biến nhất\n" +
                        "   - Nướu đỏ, sưng\n" +
                        "   - Chảy máu khi đánh răng\n\n" +
                        "2. Viêm nha chu (Periodontitis)\n" +
                        "   - Nướu tụt, răng lung lay\n" +
                        "   - Có túi nha chu\n\n" +
                        "3. Thiếu Vitamin C\n" +
                        "   - Nướu yếu, dễ chảy máu\n\n" +
                        "Khuyến nghị: Cần khám và lấy cao răng định kỳ.\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_LOOSE_TOOTH" ->
                "[TRIỆU CHỨNG RĂNG LUNG LAY]\n\n" +
                        "Nguyên nhân có thể:\n\n" +
                        "1. Viêm nha chu nặng (Advanced Periodontitis)\n" +
                        "   - Tiêu xương ổ răng\n" +
                        "   - Răng lung lay từ từ\n\n" +
                        "2. Chấn thương răng\n" +
                        "   - Do va đập, tai nạn\n\n" +
                        "3. Nghiến răng (Bruxism)\n" +
                        "   - Thường nghiến răng khi ngủ\n\n" +
                        "KHẨN CẤP: Nếu răng rất lung lay, cần khám NGAY!\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_BAD_BREATH" ->
                "[TRIỆU CHỨNG HÔI MIỆNG]\n\n" +
                        "Nguyên nhân phổ biến:\n\n" +
                        "1. Vệ sinh răng miệng kém\n" +
                        "   - Mảng bám, cao răng tích tụ\n\n" +
                        "2. Bệnh nướu/nha chu\n" +
                        "   - Viêm nướu mãn tính\n\n" +
                        "3. Sâu răng không điều trị\n" +
                        "   - Thức ăn đọng trong lỗ sâu\n\n" +
                        "4. Khô miệng\n" +
                        "   - Thiếu nước bọt\n\n" +
                        "Khuyến nghị: Lấy cao răng và khám tổng quát.\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_SENSITIVE_TEETH" ->
                "[TRIỆU CHỨNG Ê BUỐT RĂNG]\n\n" +
                        "Có thể do:\n\n" +
                        "1. Mòn men răng\n" +
                        "   - Do acid từ thức ăn/nước uống\n" +
                        "   - Đánh răng quá mạnh\n\n" +
                        "2. Tụt nướu\n" +
                        "   - Lộ chân răng (ngà răng)\n\n" +
                        "3. Sâu răng giai đoạn đầu\n" +
                        "   - Men răng bị tổn thương\n\n" +
                        "4. Nứt răng nhỏ\n" +
                        "   - Đau khi cắn\n\n" +
                        "Khuyến nghị: Dùng kem đánh răng chống ê buốt và khám kiểm tra.\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_SWOLLEN_FACE" ->
                "[TRIỆU CHỨNG SƯNG MÁ/MẶT - KHẨN CẤP!]\n\n" +
                        "Nguyên nhân có thể:\n\n" +
                        "1. Áp xe răng (Dental Abscess)\n" +
                        "   - Nhiễm trùng nặng\n" +
                        "   - Sưng đau, có thể sốt\n\n" +
                        "2. Viêm mô tế bào (Cellulitis)\n" +
                        "   - Nhiễm trùng lan rộng\n" +
                        "   - RẤT NGUY HIỂM\n\n" +
                        "3. Răng khôn mọc lệch\n" +
                        "   - Viêm quanh thân răng\n\n" +
                        "KHẨN CẤP: Sưng mặt kèm sốt, khó thở -> đến bệnh viện NGAY!\n" +
                        "Hotline KHẨN: 076.400.9726";

            case "SYMPTOM_WISDOM_TOOTH" ->
                "[VẤN ĐỀ RĂNG KHÔN]\n\n" +
                        "Các vấn đề thường gặp:\n\n" +
                        "1. Răng khôn mọc lệch/ngầm\n" +
                        "   - Đau nhức vùng góc hàm\n" +
                        "   - Khó mở miệng\n\n" +
                        "2. Viêm lợi trùm\n" +
                        "   - Sưng đỏ nướu phía sau\n" +
                        "   - Đau khi nhai\n\n" +
                        "3. Sâu răng khôn\n" +
                        "   - Khó vệ sinh\n\n" +
                        "Giải pháp: Nhổ răng khôn là phương pháp triệt để nhất.\n" +
                        "Hotline: 076.400.9726";

            default -> null;
        };
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
