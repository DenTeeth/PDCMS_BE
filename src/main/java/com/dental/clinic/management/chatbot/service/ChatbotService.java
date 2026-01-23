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

        if ("OUT_OF_SCOPE".equals(detectedId)) {
            return getOutOfScopeResponse();
        }

        if (detectedId.startsWith("PRICE")) {
            return buildPriceListResponse();
        }

        if (detectedId.startsWith("SERVICE")) {
            return handleServiceQuery(userMessage);
        }

        String symptomId = detectedId;
        if (detectedId.startsWith("SYMP") && !detectedId.startsWith("SYMPTOM_")) {
            symptomId = detectSymptomFromMessage(userMessage);
        }
        String symptomResponse = getSymptomResponse(symptomId);
        if (symptomResponse != null) {
            return symptomResponse;
        }

        Optional<ChatbotKnowledge> match = knowledgeBase.stream()
                .filter(k -> k.getKnowledgeId().equalsIgnoreCase(detectedId))
                .findFirst();

        if (match.isPresent()) {
            return match.get().getResponse();
        }

        return generateIntelligentResponse(userMessage);
    }

    /**
     * Build prompt for classification
     */
    private String buildClassificationPrompt(String userMessage, String listIds) {
        return """
                Phân loại câu hỏi của khách hàng vào ĐÚNG 1 ID.

                Danh sách ID: [%s]

                Quy tắc:
                - GREETING: chào hỏi (xin chào, hello, hi, chào bạn)
                - PRICE_LIST: hỏi giá, bảng giá, bao nhiêu tiền, chi phí
                - SERVICE_INFO: hỏi về dịch vụ cụ thể (tẩy trắng, niềng răng, implant...)
                - SYMPTOM_TOOTHACHE: đau răng, nhức răng, răng đau, buốt răng
                - SYMPTOM_BLEEDING_GUMS: chảy máu nướu, chảy máu lợi, nướu chảy máu
                - SYMPTOM_LOOSE_TOOTH: răng lung lay, răng lỏng, răng yếu
                - SYMPTOM_BAD_BREATH: hôi miệng, miệng hôi, mùi hôi
                - SYMPTOM_SENSITIVE_TEETH: ê buốt, răng nhạy cảm, buốt khi uống lạnh
                - SYMPTOM_SWOLLEN_FACE: sưng má, sưng mặt, sưng nướu
                - SYMPTOM_WISDOM_TOOTH: răng khôn, răng số 8, mọc răng khôn
                - ADDRESS: địa chỉ, ở đâu, chỉ đường
                - OUT_OF_SCOPE: không liên quan nha khoa (game, thời tiết, chính trị)

                Câu hỏi: "%s"

                CHỈ TRẢ LỜI ID, KHÔNG GIẢI THÍCH.
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
            List<DentalService> services = dentalServiceRepository.findAllActiveServicesWithCategory();
            String serviceContext = services.stream()
                    .limit(15)
                    .map(s -> s.getServiceName() + " (" + formatPrice(s.getPrice()) + ")")
                    .collect(Collectors.joining(", "));

            String prompt = """
                    Bạn là trợ lý ảo của phòng khám nha khoa DenTeeth. Tên bạn là "Em".
                    Xưng hô: Em - Anh/Chị. Giọng điệu: thân thiện, chuyên nghiệp.

                    THÔNG TIN PHÒNG KHÁM:
                    - Địa chỉ: Lô E2a-7, Đường D1, Khu Công nghệ cao, P. Long Thạnh Mỹ, TP. Thủ Đức, TPHCM
                    - Hotline: 076.400.9726
                    - Giờ làm việc: 8h-20h (Thứ 2-CN)
                    - Dịch vụ: %s

                    CÂU HỎI: "%s"

                    YÊU CẦU:
                    - Trả lời đầy đủ, hữu ích
                    - Nếu hỏi về triệu chứng: mô tả nguyên nhân + khuyên khám
                    - Nếu hỏi giá: trả lời giá + gợi ý đặt lịch
                    - Luôn kết thúc bằng hotline hoặc gợi ý hữu ích
                    - Dưới 150 từ
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
     * Detect symptom type from user message using keywords
     */
    private String detectSymptomFromMessage(String message) {
        String lowerMsg = message.toLowerCase();

        // Đau răng, nhức răng
        if (lowerMsg.contains("dau rang") || lowerMsg.contains("nhuc rang") ||
                lowerMsg.contains("đau răng") || lowerMsg.contains("nhức răng") ||
                lowerMsg.contains("dau") && lowerMsg.contains("rang")) {
            return "SYMPTOM_TOOTHACHE";
        }

        // Chảy máu nướu/lợi
        if (lowerMsg.contains("chay mau") || lowerMsg.contains("chảy máu") ||
                lowerMsg.contains("nuou") || lowerMsg.contains("nướu") ||
                lowerMsg.contains("loi") || lowerMsg.contains("lợi")) {
            return "SYMPTOM_BLEEDING_GUMS";
        }

        // Răng lung lay
        if (lowerMsg.contains("lung lay") || lowerMsg.contains("lung lay") ||
                lowerMsg.contains("rang lay") || lowerMsg.contains("răng lay")) {
            return "SYMPTOM_LOOSE_TOOTH";
        }

        // Hôi miệng
        if (lowerMsg.contains("hoi mieng") || lowerMsg.contains("hôi miệng") ||
                lowerMsg.contains("mui hoi") || lowerMsg.contains("mùi hôi")) {
            return "SYMPTOM_BAD_BREATH";
        }

        // Ê buốt răng
        if (lowerMsg.contains("e buot") || lowerMsg.contains("ê buốt") ||
                lowerMsg.contains("nhay cam") || lowerMsg.contains("nhạy cảm")) {
            return "SYMPTOM_SENSITIVE_TEETH";
        }

        // Sưng mặt/má
        if (lowerMsg.contains("sung") || lowerMsg.contains("sưng") ||
                lowerMsg.contains("mat") || lowerMsg.contains("mặt") ||
                lowerMsg.contains("ma") || lowerMsg.contains("má")) {
            return "SYMPTOM_SWOLLEN_FACE";
        }

        // Răng khôn
        if (lowerMsg.contains("rang khon") || lowerMsg.contains("răng khôn") ||
                lowerMsg.contains("moc rang") || lowerMsg.contains("mọc răng")) {
            return "SYMPTOM_WISDOM_TOOTH";
        }

        // Default to toothache if can't detect
        return "SYMPTOM_TOOTHACHE";
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
                        "temperature", 0.3,
                        "maxOutputTokens", 500));

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
