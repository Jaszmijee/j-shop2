package com.example.jshop.scheduler;

import com.example.jshop.carts_and_orders.domain.cart.Cart;
import com.example.jshop.carts_and_orders.domain.cart.CartStatus;
import com.example.jshop.carts_and_orders.repository.CartRepository;
import com.example.jshop.carts_and_orders.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartsSchedulerTest {

    @Autowired
    CartsScheduler cartsScheduler;

    @Autowired
    CartService cartService;

    @Autowired
    CartRepository cartRepository;

    @Nested
    @DisplayName("test removeEmptyCart")
    @Transactional
    class TestRemoveEmptyCart {
        @Test
        void removeEmptyCartsPositive() {
            //Given
            Cart cart1 = Cart.builder()
                    .cartStatus(CartStatus.EMPTY).
                    build();

            Cart cart2 = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING).
                    build();
            cartRepository.save(cart1);
            cartRepository.save(cart2);
            //When
            cartsScheduler.removeEmptyCarts();

            //Then
            assertEquals(1, cartRepository.findAll().size());
            assertFalse(cartRepository.existsById(cart1.getCartID()));
            assertTrue(cartRepository.existsById(cart2.getCartID()));
        }
    }
}

