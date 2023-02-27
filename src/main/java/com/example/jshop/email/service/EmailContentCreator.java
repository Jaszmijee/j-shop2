package com.example.jshop.email.service;

import com.example.jshop.carts_and_orders.domain.order.ORDER_STATUS;
import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.email.domain.Mail;
import org.springframework.stereotype.Service;

@Service
public class EmailContentCreator {

    public Mail createContent(Order order) {
        String subject = "Your order No: " + order.getOrderID();
        String message = "Your order No: " + order.getOrderID() + ", created on: " + order.getCreated() +
                " " + order.getListOfProducts() + " " +
                "\ntotal sum: " + order.getCalculatedPrice();
        if (order.getOrder_status() == ORDER_STATUS.UNPAID) {
            message += "\nYour payment is due on: " + order.getCreated().plusDays(14);
        }
        if (order.getCart().getListOfItems().isEmpty()) {
            message = "Your order No: " + order.getOrderID() + " got cancelled";
        } else {
            message += "\nYour order is paid and ready for shipment,";
        }

        message += "\nThank you for your purchase," +
                "\nYour J-Shop";

        return new Mail(
                order.getLoggedCustomer().getEmail(),
                subject,
                message,
                "admin@j-shop.com"
        );
    }

    public Mail createContentReminder(Order order) {

        String subject = "Payment reminder for order: " + order.getOrderID();
        String message = "Your order: " + order.getOrderID() + ", created on: " + order.getCreated()
                + "\n total sum: " + order.getCalculatedPrice() +
                "\n Your payment is due tomorrow. " +
                "\n Please proceed with payment, otherwise, your order will be cancelled " +
                "\n Thank you for your purchase" +
                "\n Your J-Shop";

        return new Mail(
                order.getLoggedCustomer().getEmail(),
                subject,
                message,
                "admin@j-shop.com"
        );
    }
}
