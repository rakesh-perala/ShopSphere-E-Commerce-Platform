package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.repository.OrderRepository;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {

        Order order = new Order(
                1L,
                new BigDecimal("999.99")
        );

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        Order result = orderService.createOrder(
                1L,
                new BigDecimal("999.99")
        );

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(
                new BigDecimal("999.99"),
                result.getTotalAmount()
        );
        assertEquals("CREATED", result.getStatus());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldGetOrder() {

        Order order = new Order(
                1L,
                new BigDecimal("499.99")
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Order result = orderService.getOrder(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.getOrder(99L)
        );

        assertEquals(
                "Order not found: 99",
                exception.getMessage()
        );

        verify(orderRepository).findById(99L);
    }

    @Test
    void shouldGetOrdersByUser() {

        Order order1 = new Order(
                1L,
                new BigDecimal("100.00")
        );

        Order order2 = new Order(
                1L,
                new BigDecimal("200.00")
        );

        when(orderRepository.findByUserId(1L))
                .thenReturn(List.of(order1, order2));

        List<Order> result =
                orderService.getOrdersByUser(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(1L, result.get(1).getUserId());

        verify(orderRepository).findByUserId(1L);
    }

    @Test
    void shouldUpdateOrderStatus() {

        Order order = new Order(
                1L,
                new BigDecimal("750.00")
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        Order result =
                orderService.updateOrderStatus(1L, "CONFIRMED");

        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldDeleteOrder() {

        Order order = new Order(
                1L,
                new BigDecimal("300.00")
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).findById(1L);
        verify(orderRepository).delete(order);
    }
}
