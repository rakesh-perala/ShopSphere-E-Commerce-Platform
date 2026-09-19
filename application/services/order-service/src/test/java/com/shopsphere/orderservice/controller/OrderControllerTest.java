package com.shopsphere.orderservice.controller;

import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {

        Order order = new Order(
                1L,
                new BigDecimal("999.99")
        );

        when(orderService.createOrder(
                1L,
                new BigDecimal("999.99")
        )).thenReturn(order);

        mockMvc.perform(
                post("/api/orders")
                        .param("userId", "1")
                        .param("totalAmount", "999.99")
                        .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.totalAmount").value(999.99))
        .andExpect(jsonPath("$.status").value("CREATED"));

        verify(orderService).createOrder(
                1L,
                new BigDecimal("999.99")
        );
    }

    @Test
    void shouldGetOrder() throws Exception {

        Order order = new Order(
                1L,
                new BigDecimal("499.99")
        );

        when(orderService.getOrder(1L))
                .thenReturn(order);

        mockMvc.perform(
                get("/api/orders/1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.totalAmount").value(499.99))
        .andExpect(jsonPath("$.status").value("CREATED"));

        verify(orderService).getOrder(1L);
    }

    @Test
    void shouldGetOrdersByUser() throws Exception {

        Order order1 = new Order(
                1L,
                new BigDecimal("100.00")
        );

        Order order2 = new Order(
                1L,
                new BigDecimal("200.00")
        );

        when(orderService.getOrdersByUser(1L))
                .thenReturn(List.of(order1, order2));

        mockMvc.perform(
                get("/api/orders/user/1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].userId").value(1))
        .andExpect(jsonPath("$[1].userId").value(1));

        verify(orderService).getOrdersByUser(1L);
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {

        Order order = new Order(
                1L,
                new BigDecimal("750.00")
        );

        order.setStatus("CONFIRMED");

        when(orderService.updateOrderStatus(
                1L,
                "CONFIRMED"
        )).thenReturn(order);

        mockMvc.perform(
                put("/api/orders/1/status")
                        .param("status", "CONFIRMED")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).updateOrderStatus(
                1L,
                "CONFIRMED"
        );
    }

    @Test
    void shouldDeleteOrder() throws Exception {

        doNothing()
                .when(orderService)
                .deleteOrder(1L);

        mockMvc.perform(
                delete("/api/orders/1")
        )
        .andExpect(status().isOk());

        verify(orderService).deleteOrder(1L);
    }
}
