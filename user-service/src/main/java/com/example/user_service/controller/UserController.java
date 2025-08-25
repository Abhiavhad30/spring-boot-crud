package com.example.user_service.controller;

import com.example.user_service.dto.Product;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.ProductClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductClientService productClientService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String userId,
                            @RequestParam(required = false) String username,
                            @RequestParam(required = false) String role,
                            HttpSession session,
                            Model model) {

        // ✅ Store in session if coming from login redirect
        if (userId != null) session.setAttribute("userId", userId);
        if (username != null) session.setAttribute("username", username);
        if (role != null) session.setAttribute("role", role);

        // ✅ Retrieve from session if not passed in URL
        String sessionUsername = (String) session.getAttribute("username");
        String sessionRole = (String) session.getAttribute("role");

        User user = userRepository.findByUsername(sessionUsername);
        if (user != null) {
            session.setAttribute("loggedInUser",user);
            model.addAttribute("username", user.getUsername());
            model.addAttribute("role", user.getRole());
            model.addAttribute("email", user.getEmail());
        }

        return "user/user-dashboard";
    }

    //to view all products
    @GetMapping("/products")
    public String viewProducts(Model model) {
        List<Product> products = productClientService.getAllProducts();
        model.addAttribute("products", products);
        return "user/user-products";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, @SessionAttribute("loggedInUser") User user){
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("role", "USER");
        return "user/profile";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:http://localhost:8080/login";

    }
}
