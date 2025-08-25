package com.example.admin_service.controller;

import com.example.admin_service.model.User;
import com.example.admin_service.repository.UserRepository;
import com.example.admin_service.service.ProductClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductClientService productClientService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin-dashboard"; // Just shows buttons
    }

    @GetMapping("/show-users")
    public String showUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "show-users";
    }

    @GetMapping("/add-user")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "add-user";
    }

    @PostMapping("/add-user")
    public String addUser(@ModelAttribute User user) {
        userRepository.save(user);
        return "redirect:/admin/show-users";
    }

    @GetMapping("/edit-user/{id}")
    public String editUserForm(@PathVariable String id, Model model) {

        model.addAttribute("user", userRepository.findById(id).orElse(null));
        return "edit-user";
    }

    @PostMapping("/edit-user")
    public String updateUser(@ModelAttribute User user) {
        userRepository.save(user);
        return "redirect:/admin/show-users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return "redirect:/admin/show-users";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        //return "redirect:/login"; // Redirect to login page
        return "redirect:http://localhost:8080/login"; // Gateway's login URL

    }


    @GetMapping("/counts")
    public long getCount(){
        return productClientService.getProductCount();
    }
}
