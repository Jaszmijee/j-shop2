package com.example.jshop.carts_and_orders.controller;

import com.example.jshop.carts_and_orders.domain.cart.*;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.carts_and_orders.mapper.CartMapper;
import com.example.jshop.carts_and_orders.service.CartService;
import com.example.jshop.error_handlers.exceptions.InvalidCustomerDataException;
import com.example.jshop.customer.domain.*;
import com.example.jshop.error_handlers.exceptions.*;
import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitWebConfig
@WebMvcTest(controllers = CartController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private CartMapper cartMapper;

    @Nested
    @DisplayName("test createCart /v1/j-shop/cart")
    class TestCreateCart {
        @Test
        void createCartPositive() throws Exception {
            //Given
            Cart cart = new Cart(1L, CartStatus.EMPTY, List.of(), BigDecimal.ZERO, LocalDate.of(2023, 2, 23));
            CartDto cartDto = new CartDto(1L, List.of(), CartStatus.EMPTY, BigDecimal.ZERO);
            when(cartService.createCart()).thenReturn(cart);
            when(cartMapper.mapCartToCartDto(any(Cart.class))).thenReturn(cartDto);

            //When $ Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/cart")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cartID", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts.size()", Matchers.is(0)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cartStatus", Matchers.is("EMPTY")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.calculatedPrice", Matchers.is(0)));

            verify(cartService, times(1)).createCart();
            verify(cartMapper, times(1)).mapCartToCartDto(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("test addToCart /v1/j-shop/cart/add")
    class TestAddToCart {
        @Test
        void addToCartCartNotFoundException() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            when(cartService.addToCart(anyLong(), any(CartItemsDto.class))).thenThrow(CartNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/add")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Cart with given ID Not Found", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).addToCart(anyLong(), any(CartItemsDto.class));
        }

        @Test
        void addToCartNotEnoughItemsException() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            when(cartService.addToCart(anyLong(), any(CartItemsDto.class))).thenThrow(NotEnoughItemsException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/add")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("The quantity of selected items is currently not available", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).addToCart(anyLong(), any(CartItemsDto.class));
        }

         @Test
        void addToCartProductNotFoundException() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            when(cartService.addToCart(anyLong(), any(CartItemsDto.class))).thenThrow(ProductNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/add")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Product with given Id not found", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).addToCart(anyLong(), any(CartItemsDto.class));
        }

        @Test
        void addToCartInvalidQuantityException() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            when(cartService.addToCart(anyLong(), any(CartItemsDto.class))).thenThrow(InvalidQuantityException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/add")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Provided quantity is out of range 1 - 2 147 483 647", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).addToCart(anyLong(), any(CartItemsDto.class));
        }

        @Test
        void addToCartPositive() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            CartDto cartDto = new CartDto(2L, List.of(new ItemDto(1L, "testName", 20, new BigDecimal(5.01).setScale(2, RoundingMode.HALF_EVEN))), CartStatus.PROCESSING, new BigDecimal(100.26).setScale(2, RoundingMode.HALF_EVEN));
            when(cartService.addToCart(anyLong(), any(CartItemsDto.class))).thenReturn(cartDto);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/add")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cartID", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cartStatus", Matchers.is("PROCESSING")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.calculatedPrice", Matchers.is(100.26)));

            verify(cartService, times(1)).addToCart(anyLong(), any(CartItemsDto.class));
        }
    }

    @Nested
    @DisplayName("test showCart /v1/j-shop/cart")
    class TestShowCart {
        @Test
        void showCartCartNotFoundException() throws Exception {
            //Given
            CartDto cartDto = new CartDto(2L, List.of(new ItemDto(1L, "testName", 20, new BigDecimal(5.01).setScale(2, RoundingMode.HALF_EVEN))), CartStatus.PROCESSING, new BigDecimal(100.26).setScale(2, RoundingMode.HALF_EVEN));
            when(cartService.showCart(anyLong())).thenThrow(CartNotFoundException.class);
            when(cartMapper.mapCartToCartDto(any(Cart.class))).thenReturn(cartDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/cart")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verify(cartService, times(1)).showCart(anyLong());
            verify(cartMapper, times(0)).mapCartToCartDto(any(Cart.class));
        }

        @Test
        void showCartPositive() throws Exception {
            //Given
            CartDto cartDto = new CartDto(2L, List.of(), CartStatus.PROCESSING, new BigDecimal(100.26).setScale(2, RoundingMode.HALF_EVEN));
            Cart cart = new Cart(2L, CartStatus.PROCESSING, List.of(), new BigDecimal(100.26).setScale(2, RoundingMode.HALF_EVEN), LocalDate.of(2023, 2, 23));
            when(cartService.showCart(anyLong())).thenReturn(cart);
            when(cartMapper.mapCartToCartDto(any(Cart.class))).thenReturn(cartDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/cart")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cartID", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cartStatus", Matchers.is("PROCESSING")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.calculatedPrice", Matchers.is(100.26)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts", Matchers.hasSize(0)));

            verify(cartService, times(1)).showCart(anyLong());
            verify(cartMapper, times(1)).mapCartToCartDto(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("test removeFromCart /v1/j-shop/cart/remove")
    class TestRemoveFromCart {
        @Test
        void removeFromCartCartNotFoundException() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            when(cartService.removeFromCart(anyLong(), any(CartItemsDto.class))).thenThrow(CartNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/remove")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Cart with given ID Not Found", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).removeFromCart(anyLong(), any(CartItemsDto.class));
        }

        @Test
        void removeFromCartInvalidQuantityException() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            when(cartService.removeFromCart(anyLong(), any(CartItemsDto.class))).thenThrow(InvalidQuantityException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/remove")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Provided quantity is out of range 1 - 2 147 483 647", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).removeFromCart(anyLong(), any(CartItemsDto.class));
        }

        @Test
        void removeFromCartProductNotFoundException() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            when(cartService.removeFromCart(anyLong(), any(CartItemsDto.class))).thenThrow(ProductNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/remove")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Product with given Id not found", result.getResponse().getContentAsString()));
            verify(cartService, times(1)).removeFromCart(anyLong(), any(CartItemsDto.class));
        }

        @Test
        void removeFromCartPositive() throws Exception {
            //Given
            CartItemsDto cartItemsDto = new CartItemsDto(1L, 20);
            CartDto cartDto = new CartDto(2L, List.of(new ItemDto(1L, "testName", 20, new BigDecimal("256.44"))), CartStatus.PROCESSING, new BigDecimal("256.44"));
            when(cartService.removeFromCart(anyLong(), any(CartItemsDto.class))).thenReturn(cartDto);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(cartItemsDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/remove")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cartID", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cartStatus", Matchers.is("PROCESSING")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts[0].productId", Matchers.is(1)));

            verify(cartService, times(1)).removeFromCart(anyLong(), any(CartItemsDto.class));
        }
    }

    @Nested
    @DisplayName("test cancelCart /v1/j-shop/cart")
    class TestCancelCart {

        @Test
        void cancelCartCartNotFoundException() throws Exception {
            //Given
            Mockito.doThrow(CartNotFoundException.class).when(cartService).cancelCart(anyLong());

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/cart")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Cart with given ID Not Found", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).cancelCart(anyLong());
        }

        @Test
        void cancelCartPositive() throws Exception {
            //Given
            Mockito.doNothing().when(cartService).cancelCart(anyLong());

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/cart")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(cartService, times(1)).cancelCart(anyLong());
        }
    }

    @Nested
    @DisplayName("test payForCartLogged /v1/j-shop/cart/pay/login")
    class TestPayForCartLogged {

        @Test
        void payForCartLoggedUserNotFoundException() throws Exception {
            //Given
            char[] pwwd = "password".toCharArray();
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd);
            when(cartService.payForCart(anyLong(), any(AuthenticationDataDto.class))).thenThrow(UserNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/login")
                            .param("orderId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("User does not exist", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).payForCart(anyLong(), any(AuthenticationDataDto.class));
        }

        @Test
        void payForCartLoggedAccessDeniedException() throws Exception {
            //Given
            char[] pwwd = "password".toCharArray();
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd);
            when(cartService.payForCart(anyLong(), any(AuthenticationDataDto.class))).thenThrow(AccessDeniedException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/login")
                            .param("orderId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).payForCart(anyLong(), any(AuthenticationDataDto.class));
        }

        @Test
        void payForCartLoggedOrderNotFoundException() throws Exception {
            //Given
            char[] pwwd = "password".toCharArray();
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd);
            when(cartService.payForCart(anyLong(), any(AuthenticationDataDto.class))).thenThrow(OrderNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/login")
                            .param("orderId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Order does not exist", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).payForCart(anyLong(), any(AuthenticationDataDto.class));
        }

        @Test
        void payForCartLoggedPaymentErrorException() throws Exception {
            //Given
            char[] pwwd = "password".toCharArray();
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd);
            when(cartService.payForCart(anyLong(), any(AuthenticationDataDto.class))).thenThrow(PaymentErrorException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/login")
                            .param("orderId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Failure with payment", result.getResponse().getContentAsString()));
            verify(cartService, times(1)).payForCart(anyLong(), any(AuthenticationDataDto.class));
        }

        @Test
        void payForCartLoggedPositive() throws Exception {
            //Given
            char[] pwwd = "password".toCharArray();
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd);
            OrderDtoToCustomer orderDtoToCustomer = new OrderDtoToCustomer(1L, LocalDate.of(2023, 2, 22), "dummy list", "2000.44", "PAID", null);
            when(cartService.payForCart(anyLong(), any(AuthenticationDataDto.class))).thenReturn(orderDtoToCustomer);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/login")
                            .param("orderId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderId", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPrice", Matchers.is("2000.44")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("PAID")));

            verify(cartService, times(1)).payForCart(anyLong(), any(AuthenticationDataDto.class));
        }
    }

    @Nested
    @DisplayName("payForCartUnauthenticated /v1/j-shop/cart/pay/unauthenticated")
    class TestPayForCartUnLogged {
        @Test
        void payForCartUnauthenticatedInvalidCustomerDataException() throws Exception {
            //Given
            UnauthenticatedCustomerDto unauthenticatedCustomerDto = new UnauthenticatedCustomerDto("Adam", "DDD", "ptr@ptr", "Fairy", "5", "5", "55-555", "Maputo", "Mosambique");
            when(cartService.payForCartUnauthenticatedCustomer(anyLong(), any(UnauthenticatedCustomerDto.class))).thenThrow(InvalidCustomerDataException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(unauthenticatedCustomerDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/unauthenticated")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Fields cannot be null or empty", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).payForCartUnauthenticatedCustomer(anyLong(), any(UnauthenticatedCustomerDto.class));
        }
        @Test
        void payForCartUnauthenticatedCartNotFoundException() throws Exception {
            //Given
            UnauthenticatedCustomerDto unauthenticatedCustomerDto = new UnauthenticatedCustomerDto("Adam", "DDD", "ptr@ptr", "Fairy", "5", "5", "55-555", "Maputo", "Mosambique");
            when(cartService.payForCartUnauthenticatedCustomer(anyLong(), any(UnauthenticatedCustomerDto.class))).thenThrow(CartNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(unauthenticatedCustomerDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/unauthenticated")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Cart with given ID Not Found", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).payForCartUnauthenticatedCustomer(anyLong(), any(UnauthenticatedCustomerDto.class));
        }

        @Test
        void paypayForCartUnauthenticatedPaymentErrorException() throws Exception {
            //Given
            UnauthenticatedCustomerDto unauthenticatedCustomerDto = new UnauthenticatedCustomerDto("Adam", "DDD", "ptr@ptr", "Fairy", "5", "5", "55-555", "Maputo", "Mosambique");
            when(cartService.payForCartUnauthenticatedCustomer(anyLong(), any(UnauthenticatedCustomerDto.class))).thenThrow(PaymentErrorException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(unauthenticatedCustomerDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/unauthenticated")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Failure with payment", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).payForCartUnauthenticatedCustomer(anyLong(), any(UnauthenticatedCustomerDto.class));
        }

        @Test
        void payForCartUnauthenticatedPositive() throws Exception {
            //Given
            UnauthenticatedCustomerDto unauthenticatedCustomerDto = new UnauthenticatedCustomerDto("Adam", "DDD", "ptr@ptr", "Fairy", "5", "5", "55-555", "Maputo", "Mosambique");
            OrderDtoToCustomer orderDtoToCustomer = new OrderDtoToCustomer(1L, LocalDate.of(2023, 2, 22), "dummy list", "2000.44", "PAID", null);
            when(cartService.payForCartUnauthenticatedCustomer(anyLong(), any(UnauthenticatedCustomerDto.class))).thenReturn(orderDtoToCustomer);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(unauthenticatedCustomerDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/cart/pay/unauthenticated")
                            .param("cartId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderId", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPrice", Matchers.is("2000.44")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is("PAID")));

            verify(cartService, times(1)).payForCartUnauthenticatedCustomer(anyLong(), any(UnauthenticatedCustomerDto.class));
        }
    }

    @Test
    void finalizeCart() {
    }
}