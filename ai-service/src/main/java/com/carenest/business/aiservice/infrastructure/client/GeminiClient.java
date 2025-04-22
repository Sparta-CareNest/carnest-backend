package com.carenest.business.aiservice.infrastructure.client;

import com.carenest.business.aiservice.application.dto.CaregiverSearchConditionRequestDto;
import com.carenest.business.aiservice.exception.AiException;
import com.carenest.business.aiservice.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public String translateToEnglish(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = "Translate this to English: " + content;

        // JSON 객체 구성
        ObjectNode requestBody = mapper.createObjectNode();

        ObjectNode part = mapper.createObjectNode();
        part.put("text", prompt);

        ArrayNode parts = mapper.createArrayNode();
        parts.add(part);

        ObjectNode contentNode = mapper.createObjectNode();
        contentNode.set("parts", parts);

        ArrayNode contents = mapper.createArrayNode();
        contents.add(contentNode);

        requestBody.set("contents", contents);

        HttpEntity<String> request;
        try {
            request = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create request JSON", e);
        }

        String requestUrl = URL + "?key=" + apiKey;
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, request, String.class);

        if (response.getStatusCode().is4xxClientError()) {
            throw new AiException(ErrorCode.GEMINI_CLIENT_ERROR);
        } else if (response.getStatusCode().is5xxServerError()) {
            throw new AiException(ErrorCode.GEMINI_SERVER_ERROR);
        }

        return extractTranslation(response.getBody());
    }

    private String extractTranslation(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    public CaregiverSearchConditionRequestDto extractConditions(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = buildPrompt(query);

        String body = """
                {
                  \"contents\": [
                    {
                      \"parts\": [
                        { \"text\": \"%s\" }
                      ]
                    }
                  ]
                }
                """.formatted(prompt);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        String requestUrl = URL + "?key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, request, String.class);

            if (response.getStatusCode().is4xxClientError()) {
                throw new AiException(ErrorCode.GEMINI_CLIENT_ERROR);
            } else if (response.getStatusCode().is5xxServerError()) {
                throw new AiException(ErrorCode.GEMINI_SERVER_ERROR);
            }

            return parseConditions(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Gemini 요청 실패", e);
        }
    }

    private String buildPrompt(String query) {
        return """
                너는 노인 돌봄 플랫폼의 AI 비서야. 사용자의 자연어 문장에서 지역(region), 성별(gender), 경력(experienceYears), 평균 평점(averageRating)을 추출해서 아래 JSON 형식으로 응답해.
                응답 형식:
                {
                  \"region\": \"서울\",
                  \"gender\": \"FEMALE\",
                  \"experienceYears\": 3,
                  \"averageRating\": 4.5
                }
                사용자 요청: %s
                """.formatted(query);
    }

    private CaregiverSearchConditionRequestDto parseConditions(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            return mapper.readValue(textNode.asText(), CaregiverSearchConditionRequestDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }
    }
}
