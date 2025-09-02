package com.example.order_payment.controller;

import com.example.order_payment.model.Order;
import com.example.order_payment.service.OrderService;
import com.example.order_payment.service.PayPalPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

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

    // Placeholder endpoint for payment initiation (to implement later)
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, String>> initiatePayment(@PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        String approvalUrl = payPalPaymentService.createPayment(order);

        return ResponseEntity.ok(Map.of("approval_url", approvalUrl));
    }
    // New endpoint to handle PayPal payment success redirect
    @GetMapping("/paypal/success")
    public String paymentSuccess(Model model, @RequestParam Map<String,String> params) {
        String token = params.get("token");
        String payerId = params.get("PayerID");
        model.addAttribute("message", "Payment successful! Thank you for your order.");
        return "user/purchase-confirmation";
    }

    // New endpoint to handle PayPal payment cancel redirect
    @GetMapping("/paypal/cancel")
    public String paymentCancel(Model model, @RequestParam Map<String,String> params) {
        String token = params.get("token");
        model.addAttribute("message", "Payment was cancelled. No charges were made.");
        return "user/payment-cancel"; //
    }



}

















