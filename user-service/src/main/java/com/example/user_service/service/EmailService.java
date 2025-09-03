package com.example.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmationMail(String to, String username, String orderId, double total) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Order Confirmation: " + orderId);
            message.setText("Hello " + username + ",\n\nYour order " + orderId
                    + " was placed successfully.\nTotal Amount: ₹" + total + "\n\nThank you for shopping with us!");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace(); // Consider using logger.error here for production
        }
    }
}

