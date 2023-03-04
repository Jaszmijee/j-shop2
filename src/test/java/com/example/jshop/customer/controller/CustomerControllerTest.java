package com.example.jshop.customer.controller;

import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.carts_and_orders.service.CartService;
import com.example.jshop.customer.domain.Address;
import com.example.jshop.customer.domain.AuthenticationDataDto;
import com.example.jshop.customer.domain.LoggedCustomer;
import com.example.jshop.customer.domain.LoggedCustomerDto;
import com.example.jshop.customer.service.CustomerService;
import com.example.jshop.error_handlers.exceptions.AccessDeniedException;
import com.example.jshop.error_handlers.exceptions.InvalidCustomerDataException;
import com.example.jshop.error_handlers.exceptions.OrderNotFoundException;
import com.example.jshop.error_handlers.exceptions.UserNotFoundException;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitWebConfig
@WebMvcTest(controllers = CustomerController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CartService cartService;

    @Nested
    @DisplayName("test createNewCustomer /v1/j-shop/customer")
    class TestCreateNewCustomer {
        @Test
        void testCreateNewCustomerInvalidCustomerDataException() throws Exception {
            //Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            Gson gson = new Gson();
            String jsonContent = gson.toJson(loggedCustomerDto);

            when(customerService.createNewCustomer(any(LoggedCustomerDto.class))).thenThrow(InvalidCustomerDataException.class);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Fields cannot be null or empty", result.getResponse().getContentAsString()));

            verify(customerService, times(1)).createNewCustomer(any(LoggedCustomerDto.class));
        }

        @Test
        void testCreateNewCustomerPositive() throws Exception {
            //Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            LoggedCustomer loggedCustomer = new LoggedCustomer("user", pwwd.toCharArray(), "Adam", "DDD", "ptr@ptr",
                    new Address("Fairy", "5", "5", "55-555", "Maputo", "Mozambique"));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(loggedCustomerDto);

            when(customerService.createNewCustomer(any(LoggedCustomerDto.class))).thenReturn(loggedCustomer);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(customerService, times(1)).createNewCustomer(any(LoggedCustomerDto.class));
        }
    }

    @Nested
    @DisplayName("test removeCustomer /v1/j-shop/customer")
    class TestRemoveCustomer {
        @Test
        void removeCustomerUserNotFoundException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            Mockito.doThrow(UserNotFoundException.class).when(customerService).removeCustomer(any(AuthenticationDataDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("User does not exist", result.getResponse().getContentAsString()));

            verify(customerService, times(1)).removeCustomer(any(AuthenticationDataDto.class));
        }

        @Test
        void removeCustomerAccessDeniedException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            Mockito.doThrow(AccessDeniedException.class).when(customerService).removeCustomer(any(AuthenticationDataDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(customerService, times(1)).removeCustomer(any(AuthenticationDataDto.class));
        }

        @Test
        void removeCustomerInvalidCustomerDataException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            Mockito.doThrow(InvalidCustomerDataException.class).when(customerService).removeCustomer(any(AuthenticationDataDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Fields cannot be null or empty", result.getResponse().getContentAsString()));

            verify(customerService, times(1)).removeCustomer(any(AuthenticationDataDto.class));
        }

        @Test
        void removeCustomerPositive() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            Mockito.doNothing().when(customerService).removeCustomer(any(AuthenticationDataDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(customerService, times(1)).removeCustomer(any(AuthenticationDataDto.class));
        }
    }

    @Nested
    @DisplayName("test showMyOrders /v1/j-shop/customer/show_my_orders")
    class TestShowMyOrders {
        @Test
        void showMyOrdersUserNotFoundException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            when(customerService.showMyOrders(any(AuthenticationDataDto.class))).thenThrow(UserNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/customer/show_my_orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("User does not exist", result.getResponse().getContentAsString()));

            verify(customerService, times(1)).showMyOrders(any(AuthenticationDataDto.class));
        }

        @Test
        void showMyOrdersAccessDeniedException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            when(customerService.showMyOrders(any(AuthenticationDataDto.class))).thenThrow(AccessDeniedException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/customer/show_my_orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(customerService, times(1)).showMyOrders(any(AuthenticationDataDto.class));
        }

        @Test
        void showMyOrdersInvalidCustomerDataException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            when(customerService.showMyOrders(any(AuthenticationDataDto.class))).thenThrow(InvalidCustomerDataException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/customer/show_my_orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Fields cannot be null or empty", result.getResponse().getContentAsString()));

            verify(customerService, times(1)).showMyOrders(any(AuthenticationDataDto.class));
        }

        @Test
        void showMyOrdersPositive() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            List<OrderDtoToCustomer> listOfOrders = List.of(new OrderDtoToCustomer(1L, LocalDate.of(2023, 2, 28),
                    "dummy list", "1000.24", "UNPAID", LocalDate.of(2023, 3, 13)));
            when(customerService.showMyOrders(any(AuthenticationDataDto.class))).thenReturn(listOfOrders);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/customer/show_my_orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].orderId", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].listOfProducts", Matchers.is("dummy list")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalPrice", Matchers.is("1000.24")));

            verify(customerService, times(1)).showMyOrders(any(AuthenticationDataDto.class));
        }
    }

    @Nested
    @DisplayName("test delete_my_order /v1/j-shop/customer/delete_my_order")
    class TestDelete_my_order {
        @Test
        void cancelOrderLoggedUserNotFoundException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            Mockito.doThrow(UserNotFoundException.class).when(cartService).cancelOrderLogged(anyLong(), any(AuthenticationDataDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/customer/delete_my_order")
                            .param("orderId", "4")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("User does not exist", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).cancelOrderLogged(anyLong(), any(AuthenticationDataDto.class));
        }

        @Test
        void cancelOrderLoggedAccessDeniedException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            Mockito.doThrow(AccessDeniedException.class).when(cartService).cancelOrderLogged(anyLong(), any(AuthenticationDataDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/customer/delete_my_order")
                            .param("orderId", "4")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).cancelOrderLogged(anyLong(), any(AuthenticationDataDto.class));
        }

        @Test
        void cancelOrderOrderNotFoundException() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            Mockito.doThrow(OrderNotFoundException.class).when(cartService).cancelOrderLogged(anyLong(), any(AuthenticationDataDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/customer/delete_my_order")
                            .param("orderId", "4")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Order does not exist", result.getResponse().getContentAsString()));

            verify(cartService, times(1)).cancelOrderLogged(anyLong(), any(AuthenticationDataDto.class));
        }

        @Test
        void cancelOrderPositive() throws Exception {
            //Given
            String pwwd = "password";
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());
            Mockito.doNothing().when(cartService).cancelOrderLogged(anyLong(), any(AuthenticationDataDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(authenticationDataDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/customer/delete_my_order")
                            .param("orderId", "4")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(cartService, times(1)).cancelOrderLogged(anyLong(), any(AuthenticationDataDto.class));
        }
    }
}