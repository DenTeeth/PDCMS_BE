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
            return "Da em dang gap su co ky thuat. Anh/Chi vui long goi Hotline 076.400.9726 de duoc ho tro a!";
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
        return "Xin loi, em la tro ly ao chuyen ve NHA KHOA cua phong kham DenTeeth.\n\n" +
                "Em co the giup ban:\n" +
                "- Tra cuu bang gia dich vu\n" +
                "- Tu van trieu chung rang mieng\n" +
                "- Thong tin dia chi, gio lam viec\n" +
                "- Huong dan dat lich kham\n\n" +
                "Anh/Chi co cau hoi gi ve rang mieng khong a?\n" +
                "Hotline: 076.400.9726";
    }

    /**
     * Deterministic symptom responses (same symptoms = same response ALWAYS)
     */
    private String getSymptomResponse(String symptomId) {
        return switch (symptomId) {
            case "SYMPTOM_TOOTHACHE" ->
                "[TRIEU CHUNG DAU RANG]\n\n" +
                        "Dua tren trieu chung, co the la mot trong cac van de sau:\n\n" +
                        "1. Sau rang (Dental Caries) - Pho bien nhat\n" +
                        "   - Dau khi an do ngot, nong, lanh\n" +
                        "   - Co the thay lo den tren rang\n\n" +
                        "2. Viem tuy rang (Pulpitis)\n" +
                        "   - Dau du doi, keo dai\n" +
                        "   - Dau tang ve dem\n\n" +
                        "3. Ap xe rang (Dental Abscess)\n" +
                        "   - Sung ma, dau nhuc lien tuc\n" +
                        "   - Co the sot nhe\n\n" +
                        "Khuyen nghi: Nen kham bac si som de xac dinh chinh xac nguyen nhan.\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_BLEEDING_GUMS" ->
                "[TRIEU CHUNG CHAY MAU NUOU]\n\n" +
                        "Co the la dau hieu cua:\n\n" +
                        "1. Viem nuou (Gingivitis) - Pho bien nhat\n" +
                        "   - Nuou do, sung\n" +
                        "   - Chay mau khi danh rang\n\n" +
                        "2. Viem nha chu (Periodontitis)\n" +
                        "   - Nuou tut, rang lung lay\n" +
                        "   - Co tui nha chu\n\n" +
                        "3. Thieu Vitamin C\n" +
                        "   - Nuou yeu, de chay mau\n\n" +
                        "Khuyen nghi: Can kham va lay cao rang dinh ky.\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_LOOSE_TOOTH" ->
                "[TRIEU CHUNG RANG LUNG LAY]\n\n" +
                        "Nguyen nhan co the:\n\n" +
                        "1. Viem nha chu nang (Advanced Periodontitis)\n" +
                        "   - Tieu xuong o rang\n" +
                        "   - Rang lung lay tu tu\n\n" +
                        "2. Chan thuong rang\n" +
                        "   - Do va dap, tai nan\n\n" +
                        "3. Nghien rang (Bruxism)\n" +
                        "   - Thuong nghien rang khi ngu\n\n" +
                        "KHAN CAP: Neu rang rat lung lay, can kham NGAY!\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_BAD_BREATH" ->
                "[TRIEU CHUNG HOI MIENG]\n\n" +
                        "Nguyen nhan pho bien:\n\n" +
                        "1. Ve sinh rang mieng kem\n" +
                        "   - Mang bam, cao rang tich tu\n\n" +
                        "2. Benh nuou/nha chu\n" +
                        "   - Viem nuou man tinh\n\n" +
                        "3. Sau rang khong dieu tri\n" +
                        "   - Thuc an dong trong lo sau\n\n" +
                        "4. Kho mieng\n" +
                        "   - Thieu nuoc bot\n\n" +
                        "Khuyen nghi: Lay cao rang va kham tong quat.\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_SENSITIVE_TEETH" ->
                "[TRIEU CHUNG E BUOT RANG]\n\n" +
                        "Co the do:\n\n" +
                        "1. Mon men rang\n" +
                        "   - Do acid tu thuc an/nuoc uong\n" +
                        "   - Danh rang qua manh\n\n" +
                        "2. Tut nuou\n" +
                        "   - Lo chan rang (nga rang)\n\n" +
                        "3. Sau rang giai doan dau\n" +
                        "   - Men rang bi ton thuong\n\n" +
                        "4. Nut rang nho\n" +
                        "   - Dau khi can\n\n" +
                        "Khuyen nghi: Dung kem danh rang chong e buot va kham kiem tra.\n" +
                        "Hotline: 076.400.9726";

            case "SYMPTOM_SWOLLEN_FACE" ->
                "[TRIEU CHUNG SUNG MA/MAT - KHAN CAP!]\n\n" +
                        "Nguyen nhan co the:\n\n" +
                        "1. Ap xe rang (Dental Abscess)\n" +
                        "   - Nhiem trung nang\n" +
                        "   - Sung dau, co the sot\n\n" +
                        "2. Viem mo te bao (Cellulitis)\n" +
                        "   - Nhiem trung lan rong\n" +
                        "   - RAT NGUY HIEM\n\n" +
                        "3. Rang khon moc lech\n" +
                        "   - Viem quanh than rang\n\n" +
                        "KHAN CAP: Sung mat kem sot, kho tho -> den benh vien NGAY!\n" +
                        "Hotline KHAN: 076.400.9726";

            case "SYMPTOM_WISDOM_TOOTH" ->
                "[VAN DE RANG KHON]\n\n" +
                        "Cac van de thuong gap:\n\n" +
                        "1. Rang khon moc lech/ngam\n" +
                        "   - Dau nhuc vung goc ham\n" +
                        "   - Kho mo mieng\n\n" +
                        "2. Viem loi trum\n" +
                        "   - Sung do nuou phia sau\n" +
                        "   - Dau khi nhai\n\n" +
                        "3. Sau rang khon\n" +
                        "   - Kho ve sinh\n\n" +
                        "Giai phap: Nho rang khon la phuong phap triet de nhat.\n" +
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
