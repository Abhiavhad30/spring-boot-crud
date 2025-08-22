package com.example.admin_service.controller;

import com.example.admin_service.dto.Product;
import com.example.admin_service.service.ProductClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductClientService productClientService;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard() {
        return "product/product-dashboard";
    }

    // Show add product page
    @GetMapping("/add-page")
    public String showAddPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "product/add-product";
    }

    // Handle add product form submission
    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product) {
        productClientService.addProduct(product);
        return "redirect:/admin/products/show";
    }

    // If user hits only /admin/products → redirect to dashboard
    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/admin/products/dashboard";
    }


    // Show list of products
    @GetMapping("/show")
    public String listProducts(Model model) {
        List<Product> products = productClientService.getAllProducts();
        model.addAttribute("products", products);
        return "product/show-products";
    }

    // Show update product page
    @GetMapping("/update-page/{id}")
    public String showUpdatePage(@PathVariable String id, Model model) {
        Product product = productClientService.getProductById(id);
        model.addAttribute("product", product);
        return "product/update-product";
    }

    // Handle update product form submission
    @PostMapping("/update")
    public String updateProduct(@ModelAttribute Product product) {
        productClientService.updateProduct(product.getId(), product);
        return "redirect:/admin/products/show";
    }

    // Delete product
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable String id) {
        productClientService.deleteProduct(id);
        return "redirect:/admin/products/show";
    }
}

