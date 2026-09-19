package com.shopsphere.cartservice.controller;

import com.shopsphere.cartservice.entity.Cart;
import com.shopsphere.cartservice.entity.CartItem;
import com.shopsphere.cartservice.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void shouldGetCart() throws Exception {

        Long userId = 1L;

        Cart cart = new Cart(userId);

        when(cartService.getOrCreateCart(userId))
                .thenReturn(cart);

        mockMvc.perform(
                get("/api/carts/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void shouldGetCartItems() throws Exception {

        Long userId = 1L;

        CartItem item = new CartItem(
                1L,
                100L,
                2,
                new BigDecimal("499.99")
        );

        when(cartService.getCartItems(userId))
                .thenReturn(List.of(item));

        mockMvc.perform(
                get("/api/carts/{userId}/items", userId)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(100))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    void shouldAddItemToCart() throws Exception {

        Long userId = 1L;
        Long productId = 100L;
        Integer quantity = 2;
        BigDecimal price = new BigDecimal("499.99");

        CartItem item = new CartItem(
                1L,
                productId,
                quantity,
                price
        );

        when(cartService.addItem(
                eq(userId),
                eq(productId),
                eq(quantity),
                eq(price)
        )).thenReturn(item);

        mockMvc.perform(
                post("/api/carts/{userId}/items", userId)
                        .param("productId", "100")
                        .param("quantity", "2")
                        .param("price", "499.99")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(100))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.price").value(499.99));
    }
}
