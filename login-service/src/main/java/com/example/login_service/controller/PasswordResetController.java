package com.example.login_service.controller;

import com.example.login_service.model.User;
import com.example.login_service.repository.UserRepository;
import com.example.login_service.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private Map<String, OTPDetails> otpStore = new HashMap<>();

    // Helper class for OTP and expiry
    private static class OTPDetails {
        String otp;
        long expiryTime;
        OTPDetails(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
    @GetMapping("/forgot-password")
    public String showForm(){
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam String email, RedirectAttributes redirectAttributes ,Model model) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Email not found.");
            return "redirect:/forgot-password";
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        long expiry = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes from now

        otpStore.put(email, new OTPDetails(otp , expiry));

        emailService.sendOtpEmail(user.getEmail(), otp);

        System.out.println("Generated OTP: " + otp + " for email: " + email);

        return "redirect:/verify-otp?email="+ URLEncoder.encode(email, StandardCharsets.UTF_8);
    }


    @GetMapping("/verify-otp")
    public String showVerifyForm(@ModelAttribute("email") String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp"; // HTML form to enter OTP
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            @RequestParam String email,
                            RedirectAttributes redirectAttributes) {

        OTPDetails details = otpStore.get(email);
        System.out.println("Entered OTP: " + otp + " for email: " + email);

        if (details != null) {
            System.out.println("Stored OTP: " + details.otp + ", Expiry: " + details.expiryTime +
                    ", Current time: " + System.currentTimeMillis());
        } else {
            System.out.println("No OTP found for email: " + email);
        }

        if (details != null && details.otp.equals(otp) && System.currentTimeMillis() <= details.expiryTime) {
            otpStore.remove(email);
            return "redirect:/reset-password?email=" + email;
        } else {
            if (details == null || System.currentTimeMillis() > (details != null ? details.expiryTime : 0)) {
                otpStore.remove(email);
            }
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP.");

            return "redirect:/verify-otp?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "reset-password"; // HTML form to enter new password
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(newPassword);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Password reset successful.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/reset-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        }
    }

}
