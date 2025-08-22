package com.example.product_service.controller;

import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    //get all product
    @GetMapping
    public List<Product> getAll(){
        return productRepository.findAll();
    }

    //get product by id
    @GetMapping("/{id}")
    public Optional<Product> getById(@PathVariable String id){
        return productRepository.findById(id);
    }

    //add product
    @PostMapping
    public Product addProduct(@RequestBody  Product product){
        return productRepository.save(product);
    }

    // Update product
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id, @RequestBody Product updated) {
        Optional<Product> p = productRepository.findById(id);
        if (p.isPresent()) {
            Product prod = p.get();
            prod.setName(updated.getName());
            prod.setCompany(updated.getCompany());
            prod.setQuantity(updated.getQuantity());
            prod.setPrice(updated.getPrice());
            return productRepository.save(prod);
        }
        throw new RuntimeException("Product not found");
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable String id){
        productRepository.deleteById(id);
    }

    // Filter by company or name
    @GetMapping("/search")
    public List<Product> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String company) {
        if (name != null) {
            return productRepository.findByNameContainingIgnoreCase(name);
        } else if (company != null) {
            return productRepository.findByCompany(company);
        } else {
            return productRepository.findAll();
        }
    }
}
