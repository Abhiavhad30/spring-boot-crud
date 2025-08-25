package com.example.admin_service.service;

import com.example.admin_service.dto.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ProductClientService {

    @Autowired
    private RestTemplate restTemplate;

    // Base URL configured to call product-service through API gateway
    private final String PRODUCT_SERVICE_URL = "http://product-service/api/products";

    public List<Product> getAllProducts() {
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                PRODUCT_SERVICE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );
        return response.getBody();
    }
    // Fetch product by ID
    public Product getProductById(String productId) {
        return restTemplate.getForObject(PRODUCT_SERVICE_URL + "/" + productId, Product.class);
    }


    public Product addProduct(Product product) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Product> request = new HttpEntity<>(product, headers);

        return restTemplate.postForObject(PRODUCT_SERVICE_URL, request, Product.class);
    }

    public Product updateProduct(String productId, Product product) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Product> request = new HttpEntity<>(product, headers);
        ResponseEntity<Product> response = restTemplate.exchange(
                PRODUCT_SERVICE_URL + "/" + productId,
                HttpMethod.PUT,
                request,
                Product.class
        );
        return response.getBody();
    }

    public void deleteProduct(String productId) {
        restTemplate.delete(PRODUCT_SERVICE_URL + "/" + productId);
    }

    public long getProductCount(){
        String url ="http://product-service/api/products/count";
       return restTemplate.getForObject(url , Long.class);

    }
}




