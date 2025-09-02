package com.example.order_payment.service;

import com.example.order_payment.model.Order;
import com.example.order_payment.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

//    public Order createOrder(Order order){
//        order.setStatus("CREATED");
//        return orderRepository.save(order);
//    }

    public Order createOrder(Order order){
        order.setStatus("CREATED");
        System.out.println("Saving order: " + order);
        try {
            Order savedOrder = orderRepository.save(order);
            System.out.println("Order saved with ID: " + savedOrder.getId());
            return savedOrder;
        } catch (Exception e) {
            System.err.println("Order save failed: " + e.getMessage());
            throw e;
        }
    }


    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    // Update order status (e.g., PAID, FAILED)
    public Order updateOrderStatus(String id, String status) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if(optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found");
    }
}
