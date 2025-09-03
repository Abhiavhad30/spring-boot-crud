package com.example.order_payment.service;

import com.example.order_payment.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PayPalPaymentService {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.api.base-url}")
    private String baseUrl;

    private RestTemplate restTemplate = new RestTemplate();

    private Map<String, String> paymentOrderMapping = new ConcurrentHashMap<>();


    private String getAccessToken() {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/oauth2/token",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        return (String) body.get("access_token");
    }

    // Create payment and return approval_url for redirect
    public String createPayment(Order order) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Update the return_url and cancel_url to go through API Gateway (port 8080)
        String paymentJson = "{\n" +
                "  \"intent\": \"sale\",\n" +
                "  \"redirect_urls\": {\n" +
                "    \"return_url\": \"http://localhost:8080/user/paypal/success\",\n" +
                "    \"cancel_url\": \"http://localhost:8080/user/paypal/cancel\"\n" +
                "  },\n" +
                "  \"payer\": { \"payment_method\": \"paypal\" },\n" +
                "  \"transactions\": [{\n" +
                "    \"amount\": {\n" +
                "      \"total\": \"" + order.getTotalAmount() + "\",\n" +
                "      \"currency\": \"USD\"\n" +
                "    },\n" +
                "    \"description\": \"Order Payment, order id: " + order.getId() + "\"\n" +
                "  }]\n" +
                "}";

        HttpEntity<String> request = new HttpEntity<>(paymentJson, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/v1/payments/payment",
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();

        String paymentId = (String) responseBody.get("id");
        paymentOrderMapping.put(paymentId, order.getId());


        for (Map<String, Object> link : (Iterable<Map<String, Object>>) responseBody.get("links")) {
            if ("approval_url".equals(link.get("rel"))) {
                return (String) link.get("href");
            }
        }

        throw new RuntimeException("No approval_url found in PayPal response");
    }
    // Execute approved payment
    public void executePayment(String paymentId, String payerId) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = "{ \"payer_id\": \"" + payerId + "\" }";
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/v1/payments/payment/" + paymentId + "/execute",
                request,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        String state = (String) body.get("state");
        if (!"approved".equalsIgnoreCase(state)) {
            throw new RuntimeException("Payment execution not approved, state: " + state);
        }
    }

    // Retrieve orderId from paymentId
    public String getOrderIdByPaymentId(String paymentId) {
        return paymentOrderMapping.get(paymentId);
    }


}