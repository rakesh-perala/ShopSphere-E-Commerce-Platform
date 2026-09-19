package com.shopsphere.cartservice.controller;

import com.shopsphere.cartservice.entity.Cart;
import com.shopsphere.cartservice.entity.CartItem;
import com.shopsphere.cartservice.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable Long userId) {
        return cartService.getOrCreateCart(userId);
    }

    @GetMapping("/{userId}/items")
    public List<CartItem> getCartItems(@PathVariable Long userId) {
        return cartService.getCartItems(userId);
    }

    @PostMapping("/{userId}/items")
    public CartItem addItem(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal price) {

        return cartService.addItem(
                userId,
                productId,
                quantity,
                price
        );
    }
}
