package com.example.user_service.service;

import com.example.user_service.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderClientService {

    @Autowired
    private RestTemplate restTemplate;

    // Using API Gateway URL for order-payment-service
    private static final String ORDER_PAYMENT_SERVICE_URL = "http://localhost:8080/orders";


    public String createOrderAndGetPaymentUrl(OrderDTO orderDto) {
        // Create order via API Gateway
        ResponseEntity<OrderDTO> responseEntity = restTemplate.postForEntity(
                ORDER_PAYMENT_SERVICE_URL, orderDto, OrderDTO.class);
        OrderDTO createdOrder = responseEntity.getBody();

        if (createdOrder == null || createdOrder.getId() == null) {
            throw new RuntimeException("Failed to create order or order ID is missing");
        }

        // Initiate PayPal payment, get approval URL
        String approvalUrl = restTemplate.postForObject(
                ORDER_PAYMENT_SERVICE_URL + "/" + createdOrder.getId() + "/pay",
                null,
                String.class);

        // Update the orderDto with the generated order ID for UI reference
        orderDto.setOrderIdForUI(createdOrder.getId());

        return approvalUrl;
    }

    public OrderDTO createOrder(OrderDTO orderDto) {
        return restTemplate.postForObject(ORDER_PAYMENT_SERVICE_URL, orderDto, OrderDTO.class);
    }

    public OrderDTO getOrderById(String orderId) {
        return restTemplate.getForObject(ORDER_PAYMENT_SERVICE_URL + "/" + orderId, OrderDTO.class);
    }
}
