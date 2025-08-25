package com.example.admin_service.controller;

import com.example.admin_service.model.User;
import com.example.admin_service.repository.UserRepository;
import com.example.admin_service.service.ProductClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/stats")
public class StatsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductClientService productClientService;

    @GetMapping("/counts")
    public Map<String , Long> getCounts(){
        Map<String , Long> counts = new HashMap<>();
        long userCount = userRepository.count();
        counts.put("users",userCount);

        long productCount = productClientService.getProductCount();
        counts.put("products",productCount);
        return counts;

    }
}
