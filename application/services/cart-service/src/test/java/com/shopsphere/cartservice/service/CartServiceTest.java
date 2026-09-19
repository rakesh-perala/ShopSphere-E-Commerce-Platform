package com.shopsphere.cartservice.service;

import com.shopsphere.cartservice.entity.Cart;
import com.shopsphere.cartservice.entity.CartItem;
import com.shopsphere.cartservice.repository.CartItemRepository;
import com.shopsphere.cartservice.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void shouldCreateCartWhenUserDoesNotHaveCart() {

        Long userId = 1L;

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Cart savedCart = new Cart(userId);

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(savedCart);

        Cart result = cartService.getOrCreateCart(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldReturnExistingCartWhenUserAlreadyHasCart() {

        Long userId = 1L;

        Cart existingCart = new Cart(userId);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingCart));

        Cart result = cartService.getOrCreateCart(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldAddNewItemToCart() {

        Long userId = 1L;
        Long productId = 100L;
        Integer quantity = 2;
        BigDecimal price = new BigDecimal("499.99");

        Cart cart = new Cart(userId);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(
                any(),
                eq(productId)))
                .thenReturn(Optional.empty());

        CartItem savedItem = new CartItem(
                cart.getId(),
                productId,
                quantity,
                price
        );

        when(cartItemRepository.save(any(CartItem.class)))
                .thenReturn(savedItem);

        CartItem result = cartService.addItem(
                userId,
                productId,
                quantity,
                price
        );

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(quantity, result.getQuantity());
        assertEquals(price, result.getPrice());

        verify(cartItemRepository)
                .findByCartIdAndProductId(any(), eq(productId));

        verify(cartItemRepository)
                .save(any(CartItem.class));
    }

    @Test
    void shouldIncreaseQuantityWhenProductAlreadyExists() {

        Long userId = 1L;
        Long productId = 100L;

        Cart cart = new Cart(userId);

        CartItem existingItem = new CartItem(
                cart.getId(),
                productId,
                2,
                new BigDecimal("499.99")
        );

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(
                any(),
                eq(productId)))
                .thenReturn(Optional.of(existingItem));

        when(cartItemRepository.save(existingItem))
                .thenReturn(existingItem);

        CartItem result = cartService.addItem(
                userId,
                productId,
                3,
                new BigDecimal("499.99")
        );

        assertNotNull(result);
        assertEquals(5, result.getQuantity());

        verify(cartItemRepository)
                .save(existingItem);
    }

    @Test
    void shouldReturnCartItems() {

        Long userId = 1L;

        Cart cart = new Cart(userId);

        CartItem item = new CartItem(
                cart.getId(),
                100L,
                2,
                new BigDecimal("499.99")
        );

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(cart.getId()))
                .thenReturn(List.of(item));

        List<CartItem> result = cartService.getCartItems(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getProductId());
        assertEquals(2, result.get(0).getQuantity());

        verify(cartItemRepository)
                .findByCartId(cart.getId());
    }
}
