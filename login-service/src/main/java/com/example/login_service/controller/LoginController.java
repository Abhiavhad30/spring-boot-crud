package com.example.login_service.controller;


import com.example.login_service.model.User;
import com.example.login_service.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
                              Model model,
                              HttpSession session) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("loggedInUsername",user.getUsername());
            session.setAttribute("role",user.getRole());

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                //model.addAttribute("user", user);
                //return "redirect:/admin/dashboard";
                System.out.println("Redirecting to: /admin/dashboard");

                return "redirect:http://localhost:8080/admin/dashboard";
                //return "redirect:/admin/dashboard";


            } else {
                //model.addAttribute("user", user);
//                return "redirect:/user/dashboard";
                System.out.println("Redirecting to: /user/dashboard");
                //return "redirect:http://localhost:8080/user/dashboard?username=" + user.getUsername() + "&role=" + user.getRole();
                return "redirect:http://localhost:8080/user/dashboard?userId=" + user.getId()
                        + "&username=" + user.getUsername()
                        + "&role=" + user.getRole();


                //return "redirect:http://localhost:8080/user/dashboard";
                //return "redirect:/user/dashboard";
            }
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";

    }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String role,
                                 @RequestParam String email,
                                 RedirectAttributes redirectAttributes) {
        if (userRepository.findByUsername(username) != null) {
            redirectAttributes.addFlashAttribute("error", "username already exits!!");
            return "register";
        }
        User newuser = new User();
        newuser.setUsername(username);
        newuser.setEmail(email);     // Set email

        newuser.setPassword(password);
        newuser.setRole(role);

        userRepository.save(newuser);
        redirectAttributes.addFlashAttribute("message", "Registration successful!");
        return "redirect:/login";

    }


}
