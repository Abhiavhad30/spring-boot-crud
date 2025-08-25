package com.example.user_service.service;

import com.example.user_service.model.Cart;
import com.example.user_service.model.CartItem;
import com.example.user_service.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    // ✅ Unified method name to match controller
    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            return cartRepository.save(newCart);
        });
    }

    // ✅ Add or update item in cart
    public Cart addToCart(String userId, CartItem newItem) {
        Cart cart = getCartByUserId(userId);

        // If item already exists → increase quantity
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(newItem.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
        } else {
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

//    // ✅ Clear cart
//    public void clearCart(String userId) {
//        cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
//    }
// Remove a single item from cart by productId
public void removeItem(String userId, String productId) {
    Cart cart = getCartByUserId(userId);
    cart.getItems().removeIf(item -> item.getProductId().equals(productId));
    cartRepository.save(cart);
}

}

