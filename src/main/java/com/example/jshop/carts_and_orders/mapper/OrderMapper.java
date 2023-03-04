package com.example.jshop.carts_and_orders.mapper;

import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderMapper {

    public OrderDtoToCustomer mapToOrderDtoToCustomer(Order order) {
        return new OrderDtoToCustomer(
                order.getOrderID(),
                order.getCreated(),
                order.getListOfProducts(),
                order.getCalculatedPrice().toString(),
                order.getOrder_status().toString(),
                order.getPaymentDue()
        );
    }

    public List<OrderDtoToCustomer> mapToOrderDtoToCustomerList(List<Order> listOfOrders) {
        return listOfOrders.stream().map(order -> mapToOrderDtoToCustomer(order)).toList();
    }
    }
