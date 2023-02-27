package com.example.jshop.administrator;

import com.example.jshop.carts_and_orders.domain.cart.Cart;
import com.example.jshop.carts_and_orders.domain.cart.CartStatus;
import com.example.jshop.carts_and_orders.domain.order.ORDER_STATUS;
import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.carts_and_orders.mapper.OrderMapper;
import com.example.jshop.carts_and_orders.service.OrderService;
import com.example.jshop.customer.domain.Address;
import com.example.jshop.customer.domain.LoggedCustomer;
import com.example.jshop.error_handlers.exceptions.InvalidOrderStatusException;
import com.example.jshop.error_handlers.exceptions.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTestDisplayOrders {

    @InjectMocks
    AdminService adminService;

    @Mock
    OrderService orderService;

    @Mock
    OrderMapper orderMapper;

    @Test
    void testDisplayOrdersInvalidOrderStatusException() throws InvalidOrderStatusException {
        //Given
        when(orderService.findOrders(anyString())).thenThrow(InvalidOrderStatusException.class);

        //When & Then
        assertThrows(InvalidOrderStatusException.class, () -> adminService.displayOrders("dummy"));

        verify(orderService, times(1)).findOrders(anyString());
        verify(orderMapper, times(0)).mapToOrderDtoToCustomerList(anyList());
    }

    @Test
    void testDisplayOrdersPositiveWithOptionalStatus() throws InvalidOrderStatusException, OrderNotFoundException {
        //Given
        char[] pwwd = "password".toCharArray();
        List<Order> unpaid = List.of(new Order(
                new LoggedCustomer("user", pwwd, "Adam", "DDD", "ptr@ptr",
                        new Address("Fairy", "5", "5", "55-555", "Maputo", "Mosambique")),
                new Cart(1L, CartStatus.FINALIZED, List.of(), new BigDecimal("1000.00"), LocalDate.of(2023, 2, 20)),
                LocalDate.of(2023, 2, 22), ORDER_STATUS.UNPAID, "list of products unpaid", new BigDecimal("1000.00")));
        List<Order> paid = List.of(new Order(
                new LoggedCustomer("user1", pwwd, "Adam1", "DDD1", "ptr@ptr1",
                        new Address("Fairy1", "6", "5", "66-666", "Maputo", "Mosambique")),
                new Cart(2L, CartStatus.FINALIZED, List.of(), new BigDecimal("1500.00"), LocalDate.of(2023, 2, 20)),
                LocalDate.of(2023, 2, 21), ORDER_STATUS.PAID, "list of products paid", new BigDecimal("1500.00")));
        List<Order> combined = new ArrayList<>();
        combined.addAll(paid);
        combined.addAll(unpaid);

        when(orderService.findOrders("unpaid")).thenReturn(unpaid);
        when(orderService.findOrders(null)).thenReturn(combined);

        List<OrderDtoToCustomer> unpaidOrders = List.of(new OrderDtoToCustomer(1L, LocalDate.of(2023, 2, 22), "list of products unpaid", "1000", "UNPAID", LocalDate.of(2023, 3, 20)));
        List<OrderDtoToCustomer> paidOrders = List.of(new OrderDtoToCustomer(2L, LocalDate.of(2023, 2, 21), "list of products paid", "1500", "PAID", LocalDate.of(2023, 3, 19)));
        List<OrderDtoToCustomer> combinedOrders = new ArrayList<>();
        combinedOrders.addAll(paidOrders);
        combinedOrders.addAll(unpaidOrders);

        when(orderMapper.mapToOrderDtoToCustomerList(unpaid)).thenReturn(unpaidOrders);
        when(orderMapper.mapToOrderDtoToCustomerList(combined)).thenReturn(combinedOrders);

        //When & Then
        assertEquals(1, adminService.displayOrders("unpaid").size());
        assertEquals(2, adminService.displayOrders(null).size());

        verify(orderService, times(1)).findOrders(anyString());
        verify(orderService, times(1)).findOrders(null);
        verify(orderMapper, times(2)).mapToOrderDtoToCustomerList(anyList());
    }
}
