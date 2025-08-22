package com.example.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.user_service.dto.Product;

import java.util.List;

@Service
public class ProductClientService {

    @Autowired
    private RestTemplate restTemplate;

    private final String PRODUCT_SERVICE_URL = "http://localhost:8080/api/products";


    public List<Product> getAllProducts() {
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                PRODUCT_SERVICE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {
                }
        );
        return response.getBody();
    }

    // ✅ User can only view a single product
    public Product getProductById(String productId) {
        return restTemplate.getForObject(PRODUCT_SERVICE_URL + "/" + productId, Product.class);
    }

}