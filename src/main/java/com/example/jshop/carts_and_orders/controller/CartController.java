package com.example.jshop.carts_and_orders.controller;

import com.example.jshop.carts_and_orders.domain.cart.Cart;
import com.example.jshop.error_handlers.exceptions.InvalidCustomerDataException;
import com.example.jshop.customer.domain.AuthenticationDataDto;
import com.example.jshop.customer.domain.UnauthenticatedCustomerDto;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.carts_and_orders.domain.cart.CartDto;
import com.example.jshop.carts_and_orders.domain.cart.CartItemsDto;
import com.example.jshop.error_handlers.exceptions.*;
import com.example.jshop.carts_and_orders.mapper.CartMapper;
import com.example.jshop.carts_and_orders.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/j-shop/cart")
public class CartController {

    private final CartService cartService;

    private final CartMapper cartMapper;

    @Autowired
    public CartController(CartService cartService, CartMapper cartMapper) {
        this.cartService = cartService;
        this.cartMapper = cartMapper;
    }

    @PostMapping
    ResponseEntity<CartDto> createCart() {
        return ResponseEntity.ok(cartMapper.mapCartToCartDto(cartService.createCart()));
    }

    @PutMapping("add")
    ResponseEntity<CartDto> addToCart(@RequestParam Long cartId, @RequestBody CartItemsDto cartItemsDto) throws CartNotFoundException, NotEnoughItemsException, ProductNotFoundException, InvalidQuantityException {
        return ResponseEntity.ok(cartService.addToCart(cartId, cartItemsDto));
    }

    @GetMapping
    ResponseEntity<CartDto> showCart(@RequestParam Long cartId) throws CartNotFoundException {
        Cart cart = cartService.showCart(cartId);
        return ResponseEntity.ok(cartMapper.mapCartToCartDto(cart));
    }

    @PutMapping("remove")
    ResponseEntity<CartDto> removeFromCart(@RequestParam Long cartId, @RequestBody CartItemsDto cartItemsDto) throws CartNotFoundException, ProductNotFoundException, InvalidQuantityException {
        return ResponseEntity.ok(cartService.removeFromCart(cartId, cartItemsDto));
    }

    @DeleteMapping
    ResponseEntity<Void> cancelCart(@RequestParam Long cartId) throws CartNotFoundException {
        cartService.cancelCart(cartId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("finalize/login")
    ResponseEntity<OrderDtoToCustomer> finalizeCart(@RequestParam Long cartId, @RequestBody AuthenticationDataDto authenticationDataDto) throws CartNotFoundException, UserNotFoundException, AccessDeniedException {
        return ResponseEntity.ok(cartService.finalizeCart(cartId, authenticationDataDto));
    }

    @PutMapping("pay/login")
    ResponseEntity<OrderDtoToCustomer> payForCartLogged(@RequestParam Long orderId, @RequestBody AuthenticationDataDto authenticationDataDto) throws UserNotFoundException, AccessDeniedException, OrderNotFoundException, PaymentErrorException {
        return ResponseEntity.ok(cartService.payForCart(orderId, authenticationDataDto));
    }

    @PutMapping("pay/unauthenticated")
    ResponseEntity<OrderDtoToCustomer> payForCartUnauthenticated(@RequestParam Long cartId, @RequestBody UnauthenticatedCustomerDto unauthenticatedCustomerDto) throws PaymentErrorException, CartNotFoundException, InvalidCustomerDataException {
        return ResponseEntity.ok(cartService.payForCartUnauthenticatedCustomer(cartId, unauthenticatedCustomerDto));
    }
}

