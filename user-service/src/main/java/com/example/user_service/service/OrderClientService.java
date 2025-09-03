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

    // Using API Gateway or order-payment-service base URL
    private static final String ORDER_PAYMENT_SERVICE_URL = "http://localhost:8080/orders";

    // Create order & get PayPal approval URL
    public String createOrderAndGetPaymentUrl(OrderDTO orderDto) {
        ResponseEntity<OrderDTO> responseEntity = restTemplate.postForEntity(
                ORDER_PAYMENT_SERVICE_URL, orderDto, OrderDTO.class);
        OrderDTO createdOrder = responseEntity.getBody();

        if (createdOrder == null || createdOrder.getId() == null) {
            throw new RuntimeException("Failed to create order or order ID is missing");
        }

        String approvalUrl = restTemplate.postForObject(
                ORDER_PAYMENT_SERVICE_URL + "/" + createdOrder.getId() + "/pay",
                null,
                String.class);

        orderDto.setOrderIdForUI(createdOrder.getId());
        return approvalUrl;
    }

    // Create order only
    public OrderDTO createOrder(OrderDTO orderDto) {
        return restTemplate.postForObject(ORDER_PAYMENT_SERVICE_URL, orderDto, OrderDTO.class);
    }

    // Fetch order details
    public OrderDTO getOrderById(String orderId) {
        return restTemplate.getForObject(ORDER_PAYMENT_SERVICE_URL + "/" + orderId, OrderDTO.class);
    }

    // Execute PayPal payment after approval
    public void executePayment(String paymentId, String payerId) {
        String url = ORDER_PAYMENT_SERVICE_URL + "/paypal/execute?paymentId=" + paymentId + "&PayerID=" + payerId;
        restTemplate.postForObject(url, null, Void.class);
    }

    // Update order status (e.g., "PAID", "CANCELLED")
    public void updateOrderStatus(String orderId, String status) {
        String url = ORDER_PAYMENT_SERVICE_URL + "/" + orderId + "/status?status=" + status;
        restTemplate.postForEntity(url, null, Void.class);
    }
}
