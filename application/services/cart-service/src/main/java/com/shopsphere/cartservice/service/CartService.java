package com.shopsphere.cartservice.service;

import com.shopsphere.cartservice.entity.Cart;
import com.shopsphere.cartservice.entity.CartItem;
import com.shopsphere.cartservice.repository.CartItemRepository;
import com.shopsphere.cartservice.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }

    public CartItem addItem(
            Long userId,
            Long productId,
            Integer quantity,
            BigDecimal price) {

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            return cartItemRepository.save(cartItem);
        }

        CartItem newItem = new CartItem(
                cart.getId(),
                productId,
                quantity,
                price
        );

        return cartItemRepository.save(newItem);
    }

    public List<CartItem> getCartItems(Long userId) {

        Cart cart = getOrCreateCart(userId);

        return cartItemRepository.findByCartId(cart.getId());
    }
}
