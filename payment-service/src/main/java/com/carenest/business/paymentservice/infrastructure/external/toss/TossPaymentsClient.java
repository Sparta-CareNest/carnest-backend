package com.carenest.business.paymentservice.infrastructure.external.toss;

import com.carenest.business.paymentservice.infrastructure.external.toss.dto.request.TossCancelPaymentRequest;
import com.carenest.business.paymentservice.infrastructure.external.toss.dto.request.TossPaymentConfirmRequest;
import com.carenest.business.paymentservice.infrastructure.external.toss.dto.request.TossPaymentRequest;
import com.carenest.business.paymentservice.infrastructure.external.toss.dto.response.TossPaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.UUID;

@Component
public class TossPaymentsClient {

    private final RestTemplate restTemplate;
    private final String secretKey;
    private final String apiBaseUrl;
    private final String apiVersion;

    public TossPaymentsClient(
            @Value("${toss.payments.secret-key}") String secretKey,
            @Value("${toss.payments.api-base-url}") String apiBaseUrl,
            @Value("${toss.payments.api-version}") String apiVersion) {
        this.restTemplate = new RestTemplate();
        this.secretKey = secretKey;
        this.apiBaseUrl = apiBaseUrl;
        this.apiVersion = apiVersion;
    }

    public TossPaymentResponse requestPayment(TossPaymentRequest request) {
        HttpHeaders headers = createHeaders();
        HttpEntity<TossPaymentRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                apiBaseUrl + "/payments",
                HttpMethod.POST,
                entity,
                TossPaymentResponse.class
        ).getBody();
    }

    public TossPaymentResponse getPayment(String paymentKey) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                apiBaseUrl + "/payments/" + paymentKey,
                HttpMethod.GET,
                entity,
                TossPaymentResponse.class
        ).getBody();
    }

    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount) {
        HttpHeaders headers = createHeaders();
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(orderId, amount);
        HttpEntity<TossPaymentConfirmRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                apiBaseUrl + "/payments/" + paymentKey + "/confirm",
                HttpMethod.POST,
                entity,
                TossPaymentResponse.class
        ).getBody();
    }

    public TossPaymentResponse cancelPayment(String paymentKey, TossCancelPaymentRequest request) {
        HttpHeaders headers = createHeaders();
        HttpEntity<TossCancelPaymentRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                apiBaseUrl + "/payments/" + paymentKey + "/cancel",
                HttpMethod.POST,
                entity,
                TossPaymentResponse.class
        ).getBody();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes()));
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        headers.set("Toss-Payments-Version", apiVersion);
        return headers;
    }
}