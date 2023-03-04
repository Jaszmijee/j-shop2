package com.example.jshop.customer.controller;

import com.example.jshop.customer.domain.AuthenticationDataDto;
import com.example.jshop.customer.domain.LoggedCustomerDto;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.error_handlers.exceptions.*;
import com.example.jshop.carts_and_orders.service.CartService;
import com.example.jshop.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/j-shop/customer")
public class CustomerController {

    private final CustomerService customerService;

    private final CartService cartService;

    @Autowired
    public CustomerController(CustomerService customerService, CartService cartService) {
        this.customerService = customerService;
        this.cartService = cartService;
    }

    @PostMapping
    ResponseEntity<Void> createNewCustomer(@RequestBody LoggedCustomerDto loggedCustomerDto) throws InvalidCustomerDataException {
        customerService.createNewCustomer(loggedCustomerDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    ResponseEntity<Void> removeCustomer(@RequestBody AuthenticationDataDto authenticationDataDto) throws UserNotFoundException, AccessDeniedException, InvalidCustomerDataException {
        customerService.removeCustomer(authenticationDataDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("show_my_orders")
    ResponseEntity<List<OrderDtoToCustomer>> showMyOrders(@RequestBody AuthenticationDataDto authenticationDataDto) throws UserNotFoundException, AccessDeniedException, InvalidCustomerDataException {
        return ResponseEntity.ok(customerService.showMyOrders(authenticationDataDto));
    }

    @DeleteMapping("delete_my_order")
    ResponseEntity<Void> cancelOrderLogged(@RequestParam Long orderId, @RequestBody AuthenticationDataDto authenticationDataDto) throws UserNotFoundException, AccessDeniedException, OrderNotFoundException, InvalidCustomerDataException, ProductNotFoundException {
        cartService.cancelOrderLogged(orderId, authenticationDataDto);
        return ResponseEntity.ok().build();
    }
}
