package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Long userId, BigDecimal totalAmount) {

        Order order = new Order(userId, totalAmount);

        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found: " + orderId));
    }

    public List<Order> getOrdersByUser(Long userId) {

        return orderRepository.findByUserId(userId);
    }

    public Order updateOrderStatus(Long orderId, String status) {

        Order order = getOrder(orderId);

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public void deleteOrder(Long orderId) {

        Order order = getOrder(orderId);

        orderRepository.delete(order);
    }
}
