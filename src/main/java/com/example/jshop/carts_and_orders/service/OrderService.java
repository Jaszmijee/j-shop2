package com.example.jshop.carts_and_orders.service;

import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.error_handlers.exceptions.InvalidOrderStatusException;
import com.example.jshop.error_handlers.exceptions.OrderNotFoundException;
import com.example.jshop.carts_and_orders.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Order findOrderById(Long orderId) throws OrderNotFoundException {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

    public Order findByIdAndUserName(Long orderId, String username) throws OrderNotFoundException {
        return orderRepository.findByOrderIDAndLoggedCustomer_UserName(orderId, username).orElseThrow(OrderNotFoundException::new);
    }

    public List<Order> findOrders(String order_status) throws InvalidOrderStatusException {
        if (order_status != null && !(order_status.equalsIgnoreCase("paid") || order_status.equalsIgnoreCase("unpaid")))
            throw new InvalidOrderStatusException();
        if (order_status != null){
            order_status = order_status.toUpperCase();
        }
        return orderRepository.findOrders(order_status);
    }

    public List<Order> findOrdersOfCustomer(String userName) {
        return orderRepository.findByLoggedCustomer_UserName(userName);
    }

    public void deleteOrder(Order order) {
        orderRepository.deleteById(order.getOrderID());
    }

    public List<Order> findCloseUnpaidOrders() {
        return orderRepository.findOrdersCloseTOPayment();
    }

    public List<Order> findUnpaidOrders()  {
        return orderRepository.findUnpaidOrders();
    }
}


