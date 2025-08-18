package com.example.login_service.controller;

import com.example.login_service.model.User;
import com.example.login_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;
    private Map<String, String> otpStore = new HashMap<>();


    @GetMapping("/forgot-password")
    public String showForm(){
        return "forgot-password";

    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam String username, RedirectAttributes redirectAttributes ,Model model) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Username not found.");
            return "redirect:/forgot-password";
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStore.put(username, otp);

        System.out.println("Generated OTP for user " + username + " = " + otp);

       // Pass directly to verify page
        model.addAttribute("username", username);
        model.addAttribute("otp", otp);   // ✅ send OTP
        return "verify-otp";
//         For now, show OTP directly (later replace with email/SMS)
//        redirectAttributes.addFlashAttribute("message", "Your OTP is: " + otp);
//        redirectAttributes.addFlashAttribute("username", username);
//        return "redirect:/verify-otp";
    }
    @GetMapping("/verify-otp")
    public String showVerifyForm(@ModelAttribute("username") String username, Model model) {
        model.addAttribute("username", username);
        return "verify-otp"; // HTML form to enter OTP
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String username,
                            @RequestParam String otp,
                            RedirectAttributes redirectAttributes) {
        String storedOtp = otpStore.get(username);
        if (storedOtp != null && storedOtp.equals(otp)) {
//            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/reset-password?username=" +username;
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid OTP.");
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/verify-otp";
        }
    }
    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "reset-password"; // HTML form to enter new password
    }
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setPassword(newPassword);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Password reset successful.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/reset-password";
        }
    }

}
