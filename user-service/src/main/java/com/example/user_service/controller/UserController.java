package com.example.user_service.controller;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam String username ,@RequestParam String role , Model model){
        User user= userRepository.findByUsername(username);
        if(user != null){
            model.addAttribute("username", user.getUsername());
            model.addAttribute("role", user.getRole());
            model.addAttribute("email",user.getEmail());

        }else {
            model.addAttribute("username", username);
            model.addAttribute("role", role);
            model.addAttribute("email","");
        }
        return "user-dashboard";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        //return "redirect:/login"; // Redirect to login page
        return "redirect:http://localhost:8080/login";
    }

}
