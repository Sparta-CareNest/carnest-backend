package com.carenest.business.aiservice.infrastructure.client;

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
}
