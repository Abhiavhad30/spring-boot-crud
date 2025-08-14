package com.example.login_service.controller;


import com.example.login_service.model.User;
import com.example.login_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username,
                              @RequestParam String password,
                              Model model) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                model.addAttribute("user", user);
                //return "redirect:/admin/dashboard";
                System.out.println("Redirecting to: /admin/dashboard");

                return "redirect:http://localhost:8080/admin/dashboard";

            } else {
                model.addAttribute("user", user);
//                return "redirect:/user/dashboard";
                System.out.println("Redirecting to: /user/dashboard");

                return "redirect:http://localhost:8080/user/dashboard";

            }
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }



}
