package com.example.user_service.dto;

import java.util.List;

public class OrderDTO {

    private String id;  // generated order id returned by order-payment-service
    private String userId;
    private String status;

    private double totalAmount;
    private List<OrderItemDTO> items;

    private String orderIdForUI;


    public String getOrderIdForUI() {
        return orderIdForUI;
    }

    public void setOrderIdForUI(String orderIdForUI) {
        this.orderIdForUI = orderIdForUI;
    }


    public OrderDTO() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }


}
