package com.example.user_service.controller;

import com.example.user_service.dto.OrderDTO;
import com.example.user_service.dto.OrderItemDTO;
import com.example.user_service.model.Cart;
import com.example.user_service.model.CartItem;
import com.example.user_service.service.CartService;
import com.example.user_service.service.OrderClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderClientService orderClientService;

    //  View Cart (get userId from session)
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            //return "redirect:/login";
            return "redirect:http://localhost:8080/login"; // fallback to login-service

        }
        Cart cart = cartService.getCartByUserId(userId);
        model.addAttribute("cart", cart);
        return "user/user-cart";
    }

    //  Add item to cart
    @PostMapping("/cart/add")
    public String addToCart(HttpSession session, @ModelAttribute CartItem item, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
//            return "redirect:/login";
            return "redirect:http://localhost:8080/login"; // fallback to login-service

        }
        cartService.addToCart(userId, item);
        Cart cart = cartService.getCartByUserId(userId);
        model.addAttribute("cart", cart);
        //return "redirect:/user/user-cart";
        return "user/user-cart";
    }

    // Remove a particular item from the cart by productId
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


    // Clear (empty) the entire cart
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
    // Buy entire cart - create order in order-payment-service
    @PostMapping("/cart/buy")
    public String buyCart(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String username =(String) session.getAttribute("username");
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
        orderDto.setTotalAmount(cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity()).sum());

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

        model.addAttribute("purchasedItems", cart.getItems());
        model.addAttribute("totalPrice", orderDto.getTotalAmount());
        model.addAttribute("orderId", orderDto.getOrderIdForUI()); // set in service after createOrder
        model.addAttribute("approvalUrl", approvalUrl); // pass approval URL for redirect
        model.addAttribute("message", "Purchase successful! Your Order ID: " + orderDto.getOrderIdForUI());
        model.addAttribute("username", username);

        return "user/purchase-confirmation";
    }

    @PostMapping("/cart/buy-item")
    public String buySingleItem(@RequestParam String productId,
                                @RequestParam int quantity,
                                HttpSession session,
                                Model model) {
        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        if (userId == null) {
            return "redirect:http://localhost:8080/login";
        }
        Cart cart = cartService.getCartByUserId(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
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

        model.addAttribute("purchasedItem", item);
        model.addAttribute("purchaseQty", quantity);
        model.addAttribute("totalPrice", total);
        model.addAttribute("orderId", orderDto.getOrderIdForUI());
        model.addAttribute("approvalUrl", approvalUrl);
        model.addAttribute("message", "Order placed! Order ID: " + orderDto.getOrderIdForUI() + ". Confirmation will be sent via SMS.");
        model.addAttribute("username", username);

        return "user/purchase-confirmation";
    }

}



