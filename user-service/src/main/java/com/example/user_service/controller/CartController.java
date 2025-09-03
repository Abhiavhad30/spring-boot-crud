package com.example.user_service.controller;

import com.example.user_service.dto.OrderDTO;
import com.example.user_service.dto.OrderItemDTO;
import com.example.user_service.model.Cart;
import com.example.user_service.model.CartItem;
import com.example.user_service.service.CartService;
import com.example.user_service.service.EmailService;
import com.example.user_service.service.OrderClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderClientService orderClientService;

    @Autowired
    private EmailService emailService;

    // View Cart
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:http://localhost:8080/login";
        }
        Cart cart = cartService.getCartByUserId(userId);
        model.addAttribute("cart", cart);
        return "user/user-cart";
    }

    // Add item to cart
    @PostMapping("/cart/add")
    public String addToCart(HttpSession session, @ModelAttribute CartItem item, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:http://localhost:8080/login";
        }
        cartService.addToCart(userId, item);
        Cart cart = cartService.getCartByUserId(userId);
        model.addAttribute("cart", cart);
        return "user/user-cart";
    }

    // Remove item from cart
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam String productId, HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:http://localhost:8080/login";
        }
        cartService.removeItem(userId, productId);
        Cart cart = cartService.getCartByUserId(userId);
        model.addAttribute("cart", cart);
        return "user/user-cart";
    }

    // Clear cart
    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:http://localhost:8080/login";
        }
        cartService.clearCart(userId);
        Cart cart = cartService.getCartByUserId(userId);
        model.addAttribute("cart", cart);
        return "user/user-cart";
    }

    // Buy entire cart
    @PostMapping("/cart/buy")
    public String buyCart(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");

        if (userId == null) {
            return "redirect:http://localhost:8080/login";
        }

        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            model.addAttribute("message", "Your cart is empty.");
            return "user/purchase-confirmation";
        }

        // Convert Cart to OrderDTO
        OrderDTO orderDto = new OrderDTO();
        orderDto.setUserId(userId);
        orderDto.setStatus("CREATED");
        orderDto.setTotalAmount(cart.getItems().stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum());

        List<OrderItemDTO> items = cart.getItems().stream().map(item -> {
            OrderItemDTO orderItem = new OrderItemDTO();
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            return orderItem;
        }).collect(Collectors.toList());
        orderDto.setItems(items);

        // Create order & get approval URL from payment service
        String approvalUrl = orderClientService.createOrderAndGetPaymentUrl(orderDto);

        // Clear cart after purchase
        cartService.clearCart(userId);

        // Save orderId in session for payment success callback
        session.setAttribute("lastOrderId", orderDto.getOrderIdForUI());

        model.addAttribute("purchasedItems", cart.getItems());
        model.addAttribute("totalPrice", orderDto.getTotalAmount());
        model.addAttribute("orderId", orderDto.getOrderIdForUI());
        model.addAttribute("approvalUrl", approvalUrl);
        model.addAttribute("message", "Purchase initiated! Your Order ID: " + orderDto.getOrderIdForUI());
        model.addAttribute("username", username);

        // Email will be sent on payment success, not here

        return "user/purchase-confirmation";
    }

    // Buy single item
    @PostMapping("/cart/buy-item")
    public String buySingleItem(@RequestParam String productId,
                                @RequestParam int quantity,
                                HttpSession session,
                                Model model) {
        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");

        if (userId == null) {
            return "redirect:http://localhost:8080/login";
        }

        Cart cart = cartService.getCartByUserId(userId);
        CartItem item = cart.getItems().stream().filter(i -> i.getProductId().equals(productId)).findFirst().orElse(null);
        if (item == null || item.getQuantity() < quantity) {
            model.addAttribute("message", "Invalid quantity or item not found.");
            return "user/purchase-confirmation";
        }

        OrderDTO orderDto = new OrderDTO();
        orderDto.setUserId(userId);
        orderDto.setStatus("CREATED");
        double total = item.getPrice() * quantity;
        orderDto.setTotalAmount(total);

        OrderItemDTO orderItem = new OrderItemDTO();
        orderItem.setProductId(item.getProductId());
        orderItem.setProductName(item.getProductName());
        orderItem.setPrice(item.getPrice());
        orderItem.setQuantity(quantity);

        orderDto.setItems(Collections.singletonList(orderItem));

        // Create order & get approval URL from payment service
        String approvalUrl = orderClientService.createOrderAndGetPaymentUrl(orderDto);

        // Update cart item quantity & save cart
        item.setQuantity(item.getQuantity() - quantity);
        if (item.getQuantity() == 0) {
            cart.getItems().remove(item);
        }
        cartService.saveCart(cart);

        // Save orderId in session for payment success callback
        session.setAttribute("lastOrderId", orderDto.getOrderIdForUI());

        model.addAttribute("purchasedItem", item);
        model.addAttribute("purchaseQty", quantity);
        model.addAttribute("totalPrice", total);
        model.addAttribute("orderId", orderDto.getOrderIdForUI());
        model.addAttribute("approvalUrl", approvalUrl);
        model.addAttribute("message", "Order placed! Order ID: " + orderDto.getOrderIdForUI() + ". Confirmation will be sent via SMS.");
        model.addAttribute("username", username);


        return "user/purchase-confirmation";
    }

    // Payment Success endpoint - PayPal redirects here after success
    @GetMapping("/paypal/success")
    public String paymentSuccess(@RequestParam Map<String, String> params, HttpSession session, Model model) {
        String paymentId = params.get("paymentId");
        String payerId = params.get("PayerID");

        String orderId = (String) session.getAttribute("lastOrderId");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");

        try {
            // Execute payment (calls order-payment service)
            orderClientService.executePayment(paymentId, payerId);

            // Update order status to PAID
            orderClientService.updateOrderStatus(orderId, "PAID");

            // Fetch actual order total
            OrderDTO order = orderClientService.getOrderById(orderId);
            double totalAmount = order != null ? order.getTotalAmount() : 0.0;

            // Send email notification
            emailService.sendOrderConfirmationMail(email, username, orderId, totalAmount);

            model.addAttribute("message", "Payment successful! Thank you for your order.");
            model.addAttribute("orderId", orderId);

            return "user/purchase-confirmation";
        } catch (Exception e) {
            model.addAttribute("message", "Payment processing failed: " + e.getMessage());
            return "user/payment-cancel";
        }
    }

    // Payment Cancel endpoint - PayPal redirects here after cancel
    @GetMapping("/paypal/cancel")
    public String paymentCancel(Model model) {
        model.addAttribute("message", "Payment was cancelled. No charges were made.");
        return "user/payment-cancel";
    }
}













