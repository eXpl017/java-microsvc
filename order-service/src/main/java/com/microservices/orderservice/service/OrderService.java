package com.microservices.orderservice.service;

import com.microservices.orderservice.entity.Order;
import com.microservices.orderservice.exception.OrderNotFoundException;
import com.microservices.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public Order createOrder(Order order) {
        order.setStatus("pending");
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order updated) {
        Order existing = getOrderById(id);
        existing.setItem(updated.getItem());
        existing.setQuantity(updated.getQuantity());
        existing.setStatus(updated.getStatus());
        return orderRepository.save(existing);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
