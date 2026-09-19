package com.shopsphere.orderservice.controller;

import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order createOrder(
            @RequestParam Long userId,
            @RequestParam BigDecimal totalAmount) {

        return orderService.createOrder(userId, totalAmount);
    }

    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {

        return orderService.getOrder(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {

        return orderService.getOrdersByUser(userId);
    }

    @PutMapping("/{orderId}/status")
    public Order updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {

        return orderService.updateOrderStatus(orderId, status);
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable Long orderId) {

        orderService.deleteOrder(orderId);
    }
}
