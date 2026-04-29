package com.microservices.orderservice.service;

import com.microservices.orderservice.entity.Order;
import com.microservices.orderservice.exception.OrderNotFoundException;
import com.microservices.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public OrderService(OrderRepository orderRepository,
                        WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder;
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
        // save order first with PENDING status
        order.setStatus("PENDING");
        Order saved = orderRepository.save(order);

        // call payment-service to process payment
        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://payment-service/payments/process")
                    .bodyValue(new PaymentRequest(saved.getId(), 100.0))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            saved.setStatus("PAID");
        } catch (Exception e) {
            saved.setStatus("PAYMENT_FAILED");
        }

        return orderRepository.save(saved);
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

    // simple inner class to send payment request
    static class PaymentRequest {
        public Long orderId;
        public Double amount;

        public PaymentRequest(Long orderId, Double amount) {
            this.orderId = orderId;
            this.amount = amount;
        }
    }
}
