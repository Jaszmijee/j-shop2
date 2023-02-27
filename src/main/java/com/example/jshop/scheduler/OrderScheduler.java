package com.example.jshop.scheduler;

import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.email.service.EmailContentCreator;
import com.example.jshop.error_handlers.exceptions.OrderNotFoundException;
import com.example.jshop.carts_and_orders.service.CartService;
import com.example.jshop.carts_and_orders.service.OrderService;
import com.example.jshop.email.service.SimpleEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderScheduler {

    @Autowired
    OrderService orderService;

    @Autowired
    SimpleEmailService emailService;

    @Autowired
    CartService cartService;
    @Autowired
    EmailContentCreator contentCreator;

    @Scheduled(cron = "0 0 0 * * ?")
    public void remindAboutPayment() {
        List<Order> unpaidOrders = orderService.findCloseUnpaidOrders();
        for (Order order : unpaidOrders) {
            emailService.send(contentCreator.createContentReminder(order));
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void removeUnpaidOrders() throws  OrderNotFoundException  {
        List<Order> unpaidOrders = orderService.findUnpaidOrders();
        for (Order orderToCancel : unpaidOrders) {
              cartService.cancelOrder(orderToCancel.getOrderID());
        }
    }
}

