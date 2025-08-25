package com.example.user_service.controller;

import com.example.user_service.model.Cart;
import com.example.user_service.model.CartItem;
import com.example.user_service.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class CartController {

    @Autowired
    private CartService cartService;

    //  View Cart (get userId from session)
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
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
            return "redirect:/login";
        }
        cartService.addToCart(userId, item);
        Cart cart = cartService.getCartByUserId(userId);
        model.addAttribute("cart", cart);
        //return "redirect:/user/user-cart";
        return "user/user-cart";
    }

    @PostMapping("/cart/remove")
    public String removeItemFromCart(HttpSession session, @RequestParam("productId") String productId) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        cartService.removeItem(userId, productId); // See note below
        return "redirect:/user/cart";
    }

//    @PostMapping("/cart/clear")
//    public String clearCart(HttpSession session) {
//        String userId = (String) session.getAttribute("userId");
//        if (userId == null) {
//            return "redirect:/login";
//        }
//        cartService.clearCart(userId);
//        return "redirect:/user/cart";
//    }



}



