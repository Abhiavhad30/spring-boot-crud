package com.example.user_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        model.addAttribute("message","Welcome to the User Dashboard");
        return "user-dashboard";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        //return "redirect:/login"; // Redirect to login page
        return "redirect:http://localhost:8080/login";
    }

}
