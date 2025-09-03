package com.example.order_payment.controller;

import com.example.order_payment.model.Order;
import com.example.order_payment.service.OrderService;
import com.example.order_payment.service.PayPalPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayPalPaymentService payPalPaymentService;

    // Create new order
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order created = orderService.createOrder(order);
        return ResponseEntity.ok(created);
    }

    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update order status (e.g., after payment success)
    @PostMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable String orderId, @RequestParam String status) {
        try {
            Order updated = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Payment initiation (create PayPal payment)
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, String>> initiatePayment(@PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        String approvalUrl = payPalPaymentService.createPayment(order);

        return ResponseEntity.ok(Map.of("approval_url", approvalUrl));
    }

    @PostMapping("/paypal/execute")
    public ResponseEntity<?> executePayment(@RequestParam String paymentId, @RequestParam String PayerID) {
        try {
            payPalPaymentService.executePayment(paymentId, PayerID);

            String orderId = payPalPaymentService.getOrderIdByPaymentId(paymentId);

            orderService.updateOrderStatus(orderId, "PAID");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Payment execution failed: ", e);
            return ResponseEntity.status(500).body("Payment execution failed: " + e.getMessage());
        }
    }
}

