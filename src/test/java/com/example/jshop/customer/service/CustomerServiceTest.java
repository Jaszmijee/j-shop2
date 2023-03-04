package com.example.jshop.customer.service;

import com.example.jshop.carts_and_orders.domain.cart.Cart;
import com.example.jshop.carts_and_orders.domain.cart.CartStatus;
import com.example.jshop.carts_and_orders.domain.cart.Item;
import com.example.jshop.carts_and_orders.domain.order.ORDER_STATUS;
import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.carts_and_orders.repository.CartRepository;
import com.example.jshop.carts_and_orders.repository.ItemRepository;
import com.example.jshop.carts_and_orders.service.OrderService;
import com.example.jshop.customer.domain.Address;
import com.example.jshop.customer.domain.AuthenticationDataDto;
import com.example.jshop.customer.domain.LoggedCustomer;
import com.example.jshop.customer.domain.LoggedCustomerDto;
import com.example.jshop.customer.repository.CustomerRepository;
import com.example.jshop.error_handlers.exceptions.AccessDeniedException;
import com.example.jshop.error_handlers.exceptions.InvalidCustomerDataException;
import com.example.jshop.error_handlers.exceptions.UserNotFoundException;
import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.repository.CategoryRepository;
import com.example.jshop.warehouse_and_products.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerServiceTest {

    @Autowired
    CustomerService customerService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    CartRepository cartRepository;

    @Nested
    @DisplayName("test updateCustomer")
    @Transactional
    class TestUpdateCustomer {
        @Test
        void updateCustomerPositive() {
            //Given
            LoggedCustomer loggedCustomer = new LoggedCustomer();
            customerRepository.save(loggedCustomer);
            loggedCustomer.getListOfOrders().add(new Order());

            //When
            LoggedCustomer searchedCustomer = customerService.updateCustomer(loggedCustomer);

            //Then
            assertEquals(1, searchedCustomer.getListOfOrders().size());
        }
    }

    @Nested
    @DisplayName("test createNewCustomer")
    @Transactional
    class TestCreateNewCustomer {
        @Test
        void createNewCustomerInvalidCustomerDataException() {
            //Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");

            //When & Then
            assertThrows(InvalidCustomerDataException.class, () -> customerService.createNewCustomer(loggedCustomerDto));
        }

        @Test
        void createNewCustomerPositive() {
            //Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");

            //When & Then
            try {
                LoggedCustomer loggedCustomer = customerService.createNewCustomer(loggedCustomerDto);
                assertEquals(0, loggedCustomer.getListOfOrders().size());
                assertFalse(Arrays.equals(loggedCustomerDto.getPassword().toCharArray(), loggedCustomer.getPassword()));
                assertEquals("Maputo", loggedCustomer.getAddress().getCity());
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @DisplayName("test verifyLogin")
    @Transactional
    class TestVerifyLogin {
        @Test
        void verifyLoginInvalidCustomerDataException() {
            //Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            String userName = "user";
            char[] password = pwwd.toCharArray();

            //When & Then
            assertThrows(InvalidCustomerDataException.class, () -> customerService.verifyLogin(null, password));
            assertThrows(InvalidCustomerDataException.class, () -> customerService.verifyLogin("", password));
            assertThrows(InvalidCustomerDataException.class, () -> customerService.verifyLogin(userName, null));
            assertThrows(InvalidCustomerDataException.class, () -> customerService.verifyLogin(userName, new char[]{}));
        }

        @Test
        void verifyLoginUserNotFoundException() {
            //Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }

            char[] password = pwwd.toCharArray();

            //When & Then
            assertThrows(UserNotFoundException.class, () -> customerService.verifyLogin("user1", password));
        }

        @Test
        void verifyLoginAccessDeniedException() {
            //Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }

            char[] password = "something".toCharArray();

            //When & Then
            assertThrows(AccessDeniedException.class, () -> customerService.verifyLogin("user", password));
        }

        @Test
        void verifyLoginPositive() {
            //Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }

            //When & Then
            assertDoesNotThrow(() -> customerService.verifyLogin("user", "password".toCharArray()));
        }
    }

    @Nested
    @DisplayName("test removeCustomer")
    @Transactional
    class TestRemoveCustomer {
        @Test
        void removeCustomerInvalidCustomerDataException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto(null, "password".toCharArray());
            AuthenticationDataDto authenticationDataDto1 = new AuthenticationDataDto("", "password".toCharArray());
            AuthenticationDataDto authenticationDataDto2 = new AuthenticationDataDto("user", null);
            AuthenticationDataDto authenticationDataDto3 = new AuthenticationDataDto("user", new char[]{});

            //When & Then
            assertThrows(InvalidCustomerDataException.class, () -> customerService.removeCustomer(authenticationDataDto));
            assertThrows(InvalidCustomerDataException.class, () -> customerService.removeCustomer(authenticationDataDto1));
            assertThrows(InvalidCustomerDataException.class, () -> customerService.removeCustomer(authenticationDataDto2));
            assertThrows(InvalidCustomerDataException.class, () -> customerService.removeCustomer(authenticationDataDto3));
        }

        @Test
        void removeCustomerUserNotFoundException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user1", "password".toCharArray());

            //When & Then
            assertThrows(UserNotFoundException.class, () -> customerService.removeCustomer(authenticationDataDto));
        }

        @Test
        void removeCustomerAccessDeniedException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", "something".toCharArray());

            //When & Then
            assertThrows(AccessDeniedException.class, () -> customerService.removeCustomer(authenticationDataDto));
        }

        @Test
        void removeCustomerPositive() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            Long customerId = null;
            try {
                LoggedCustomer loggedCustomer = customerService.createNewCustomer(loggedCustomerDto);
                customerId = loggedCustomer.getCustomerID();
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }

            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());

            //When
            try {
                customerService.removeCustomer(authenticationDataDto);
            } catch (UserNotFoundException | AccessDeniedException | InvalidCustomerDataException e) {
                e.printStackTrace();

                //Then
                assertFalse(customerRepository.existsById(customerId));
            }
        }
    }

    @Nested
    @DisplayName("test deleteUnauthenticatedCustomer")
    @Transactional
    class TestDeleteUnauthenticatedCustomer {
        // Given
        @Test
        void deleteUnauthenticatedCustomer() {
            LoggedCustomer unauthenticatedCustomer = new LoggedCustomer(null, null, "Adam", "DDD", "ptr@ptr",
                    new Address("Fairy", "5", "5", "55-555", "Maputo", "Mozambique"));
            customerRepository.save(unauthenticatedCustomer);

            // When
            customerService.deleteUnauthenticatedCustomer(unauthenticatedCustomer.getCustomerID());

            //Then
            assertFalse(customerRepository.existsById(unauthenticatedCustomer.getCustomerID()));
        }
    }

    @Nested
    @DisplayName("test showMyOrders")
    @Transactional
    class TestShowMyOrders {
        @Test
        void showMyOrdersInvalidCustomerDataException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            LoggedCustomer loggedCustomer = null;
            try {
                loggedCustomer = customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }

            Cart cart = new Cart();
            Order order = new Order(loggedCustomer, cart, LocalDate.of(2023, 3, 1), ORDER_STATUS.UNPAID, "dummy list", new BigDecimal("282.14"));
            orderService.save(order);

            AuthenticationDataDto customerDto = new AuthenticationDataDto("", "password".toCharArray());

            //When & Then
            assertThrows(InvalidCustomerDataException.class, () -> customerService.showMyOrders(customerDto));
        }

        @Test
        void showMyOrdersAccessDeniedException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");

            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }

            AuthenticationDataDto customerDto = new AuthenticationDataDto("user", "something".toCharArray());

            //When & Then
            assertThrows(AccessDeniedException.class, () -> customerService.showMyOrders(customerDto));
        }

        @Test
        void showMyOrdersUserNotFoundException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");

            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }

            AuthenticationDataDto customerDto = new AuthenticationDataDto("user1", "password".toCharArray());

            //When & Then
            assertThrows(UserNotFoundException.class, () -> customerService.showMyOrders(customerDto));
        }

        @Test
        void showMyOrdersPositive() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            LoggedCustomer loggedCustomer = null;
            try {
                loggedCustomer = customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }

            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(new ArrayList<>())
                    .build();
            cartRepository.save(cart);
            Item item = Item.builder()
                    .product(product)
                    .quantity(10)
                    .cart(cart)
                    .build();
            itemRepository.save(item);
            cart.getListOfItems().add(item);
            cartRepository.save(cart);

            Order order = new Order(loggedCustomer, cart, LocalDate.of(2023, 3, 1), ORDER_STATUS.UNPAID, "dummy list", new BigDecimal("282.14"));
            orderService.save(order);

            AuthenticationDataDto customerDto = new AuthenticationDataDto("user", "password".toCharArray());

            //When & Then
            try {
                List<OrderDtoToCustomer> orderDtoToCustomerList = customerService.showMyOrders(customerDto);
                assertEquals(1, orderDtoToCustomerList.size());
                assertEquals("282.14", orderDtoToCustomerList.get(0).getTotalPrice());
                assertEquals("dummy list", orderDtoToCustomerList.get(0).getListOfProducts());
            //    assertEquals(LocalDate.of(2023, 3, 1).plusDays(14), orderDtoToCustomerList.get(0).getPaymentDue());

            } catch (UserNotFoundException | AccessDeniedException | InvalidCustomerDataException e) {
                e.printStackTrace();
            }
        }
    }
}
