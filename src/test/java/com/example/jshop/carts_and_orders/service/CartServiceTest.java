package com.example.jshop.carts_and_orders.service;

import com.example.jshop.carts_and_orders.domain.cart.*;
import com.example.jshop.carts_and_orders.domain.order.ORDER_STATUS;
import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.carts_and_orders.repository.CartRepository;
import com.example.jshop.carts_and_orders.repository.ItemRepository;
import com.example.jshop.carts_and_orders.repository.OrderRepository;
import com.example.jshop.customer.domain.*;
import com.example.jshop.customer.repository.CustomerRepository;
import com.example.jshop.customer.service.CustomerService;
import com.example.jshop.error_handlers.exceptions.*;
import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.domain.warehouse.Warehouse;
import com.example.jshop.warehouse_and_products.repository.CategoryRepository;
import com.example.jshop.warehouse_and_products.repository.ProductRepository;
import com.example.jshop.warehouse_and_products.repository.WarehouseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    CustomerService customerService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Nested
    @Transactional
    @DisplayName("test createCart")
    class TestCreateCart {

        @Test
        void createCartPositive() {
            //When
            Cart cart = cartService.createCart();

            //Then
            assertEquals("EMPTY", cart.getCartStatus().toString());
            assertTrue(cart.getListOfItems().isEmpty());
        }
    }

    @Nested
    @Transactional
    @DisplayName("test addToCart")
    class TestAddToCart {

        @Test
        void addToCartCartNotFoundException() {
            // Given
            Cart cart = Cart.builder()
                    .build();
            Cart cart1 = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .build();
            cartRepository.save(cart);
            cartRepository.save(cart1);
            CartItemsDto cartItemsDto = new CartItemsDto(15L, 10);

            //When & Then
            assertThrows(CartNotFoundException.class, () -> cartService.addToCart(cart.getCartID() + 1, cartItemsDto));
            assertThrows(CartNotFoundException.class, () -> cartService.addToCart(cart1.getCartID(), cartItemsDto));
        }

        @Test
        void addToCartInvalidQuantityException() {
            // Given
            Cart cart = Cart.builder()
                    .build();
            cartRepository.save(cart);
            CartItemsDto cartItemsDto = new CartItemsDto(15L, -10);

            //When & Then
            assertThrows(InvalidQuantityException.class, () -> cartService.addToCart(cart.getCartID(), cartItemsDto));
        }

        @Test
        void addToCartProductNotFoundException() {
            // Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            Product product1 = new Product("testName1", "testDescription1", category, new BigDecimal("10.00"));
            category.getListOfProducts().add(product);
            category.getListOfProducts().add(product1);
            categoryRepository.save(category);
            productRepository.save(product);
            productRepository.save(product1);
            Warehouse warehouse = new Warehouse(product, 0);
            warehouseRepository.save(warehouse);
            CartItemsDto cartItemsDto = new CartItemsDto(product.getProductID(), 20);
            CartItemsDto cartItemsDto1 = new CartItemsDto(product1.getProductID(), 20);
            Cart cart = Cart.builder()
                    .build();
            cartRepository.save(cart);

            //When & Then
            assertThrows(ProductNotFoundException.class, () -> cartService.addToCart(cart.getCartID(), cartItemsDto));
            assertThrows(ProductNotFoundException.class, () -> cartService.addToCart(cart.getCartID(), cartItemsDto1));
        }

        @Test
        void addToCartNotEnoughItemsException() {
            // Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Warehouse warehouse = new Warehouse(product, 2);
            warehouseRepository.save(warehouse);
            Cart cart = Cart.builder()
                    .build();
            cartRepository.save(cart);
            CartItemsDto cartItemsDto = new CartItemsDto(product.getProductID(), 20);

            //When & Then
            assertThrows(NotEnoughItemsException.class, () -> cartService.addToCart(cart.getCartID(), cartItemsDto));
        }

        @Test
        void addToCartPositive() {
            // Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Warehouse warehouse = new Warehouse(product, 20);
            warehouseRepository.save(warehouse);
            CartItemsDto cartItemsDto = new CartItemsDto(product.getProductID(), 5);
            Item item = Item.builder()
                    .product(product)
                    .quantity(5)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.EMPTY)
                    .listOfItems(list)
                    .created(LocalDate.of(2023, 2, 23))
                    .build();
            cartRepository.save(cart);

            //When & Then
            try {
                CartDto cartDto = cartService.addToCart(cart.getCartID(), cartItemsDto);
                assertEquals("PROCESSING", cartDto.getCartStatus().toString());
                assertEquals(1, cartDto.getListOfProducts().size());
                assertEquals(10, cartDto.getListOfProducts().get(0).getProductQuantity());
                assertEquals("50.00", cartDto.getCalculatedPrice().toString());
                int quantityInWarehouse = warehouse.getProductQuantity();
                assertEquals(15, quantityInWarehouse);
            } catch (CartNotFoundException | NotEnoughItemsException | ProductNotFoundException |
                     InvalidQuantityException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test showCart")
    class TestShowCart {
        @Test
        void showCartCartNotFoundException() {
            //Given
            Cart cart = Cart.builder()
                    .build();
            cartRepository.save(cart);

            //When & Then
            assertThrows(CartNotFoundException.class, () -> cartService.showCart(cart.getCartID() + 1));
        }

        @Test
        void showCartPositive() {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Item item = Item.builder()
                    .product(product)
                    .quantity(5)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(list)
                    .created(LocalDate.of(2023, 2, 23))
                    .calculatedPrice(new BigDecimal("25.00"))
                    .build();
            cartRepository.save(cart);
            item.setCart(cart);
            itemRepository.save(item);

            //When & Then
            try {
                assertInstanceOf(Cart.class, cartService.showCart(cart.getCartID()));
                assertEquals(1, cartService.showCart(cart.getCartID()).getListOfItems().size());
                assertEquals("PROCESSING", cartService.showCart(cart.getCartID()).getCartStatus().toString());
                assertEquals("25.00", cartService.showCart(cart.getCartID()).getCalculatedPrice().toString());
            } catch (CartNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test removeFromCart")
    class TestRemoveFromCart {

        @Test
        void removeFromCartInvalidQuantityException() {
            //Given
            Cart cart = Cart.builder()
                    .build();
            cartRepository.save(cart);
            CartItemsDto cartItemsDto = new CartItemsDto(2L, -10);

            //When & Then
            assertThrows(InvalidQuantityException.class, () -> cartService.removeFromCart((cart.getCartID()), cartItemsDto));
        }


        @Test
        void removeFromCartCartNotFoundException() {
            //Given
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .build();
            cartRepository.save(cart);
            CartItemsDto cartItemsDto = new CartItemsDto(2L, 10);

            Cart cart1 = Cart.builder()
                    .cartStatus(CartStatus.EMPTY)
                    .build();
            cartRepository.save(cart1);

            //When & Then
            assertThrows(CartNotFoundException.class, () -> cartService.removeFromCart((Long.MIN_VALUE), cartItemsDto));
            assertThrows(CartNotFoundException.class, () -> cartService.removeFromCart((cart1.getCartID()), cartItemsDto));
        }

        @Test
        void removeFromCartProductNotFoundException() {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            CartItemsDto cartItemsDto = new CartItemsDto(product.getProductID() + 1, 5);
            Item item = Item.builder()
                    .product(product)
                    .quantity(5)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(list)
                    .build();
            cartRepository.save(cart);
            Long cartId = cart.getCartID();

            //When & Then
            assertThrows(ProductNotFoundException.class, () -> cartService.removeFromCart(cartId, cartItemsDto));
        }

        @Test
        void removeFromCartPositive() {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Warehouse warehouse = new Warehouse(product, 20);
            warehouseRepository.save(warehouse);
            CartItemsDto cartItemsDto = new CartItemsDto(product.getProductID(), 5);
            Item item = Item.builder()
                    .product(product)
                    .quantity(10)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(list)
                    .build();
            cartRepository.save(cart);

            //When & Then
            try {
                CartDto cartDto = cartService.removeFromCart(cart.getCartID(), cartItemsDto);
                assertEquals("PROCESSING", cartDto.getCartStatus().toString());
                assertEquals(1, cartDto.getListOfProducts().size());
                assertEquals(5, cartDto.getListOfProducts().get(0).getProductQuantity());
                assertEquals("25.00", cartDto.getCalculatedPrice().toString());
                int quantityInWarehouse = warehouse.getProductQuantity();
                assertEquals(25, quantityInWarehouse);
            } catch (CartNotFoundException | ProductNotFoundException |
                     InvalidQuantityException e) {
                e.printStackTrace();
            }
        }

        @Test
        void removeFromCartEverythingPositive() {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Warehouse warehouse = new Warehouse(product, 20);
            warehouseRepository.save(warehouse);
            Item item = Item.builder()
                    .product(product)
                    .quantity(10)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(list)
                    .build();
            cartRepository.save(cart);
            CartItemsDto cartItemsDto = new CartItemsDto(product.getProductID(), 10);

            //When & Then
            try {
                CartDto cartDto = cartService.removeFromCart(cart.getCartID(), cartItemsDto);
                assertEquals("EMPTY", cartDto.getCartStatus().toString());
                assertEquals(0, cartDto.getListOfProducts().size());
                assertEquals("0", cartDto.getCalculatedPrice().toString());
                int quantityInWarehouse = warehouse.getProductQuantity();
                assertEquals(30, quantityInWarehouse);
            } catch (CartNotFoundException | ProductNotFoundException |
                     InvalidQuantityException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test cancelCart")
    class TestCancelCart {
        @Test
        void cancelCartCartNotFoundException() {
            //Given
            Cart cart = Cart.builder()
                    .build();
            cartRepository.save(cart);

            //When & Then
            assertThrows(CartNotFoundException.class, () -> cartService.cancelCart(cart.getCartID() + 1));
        }

        @Test
        void cancelCartPositive() {
            // Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Warehouse warehouse = new Warehouse(product, 20);
            warehouseRepository.save(warehouse);
            Item item = Item.builder()
                    .product(product)
                    .quantity(8)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);

            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(list)
                    .build();
            cartRepository.save(cart);

            //When & Then
            try {
                cartService.cancelCart(cart.getCartID());
                assertFalse(cartRepository.existsById(cart.getCartID()));
                assertEquals(28, warehouse.getProductQuantity());
            } catch (CartNotFoundException | ProductNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test finalizeCart")
    class TestFinalizeCart {
        @Test
        void finalizeCartUserNotFoundException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
             customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .build();
            cartRepository.save(cart);
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("anotherUser", pwwd.toCharArray());

            //When & Then
            assertThrows(UserNotFoundException.class, () -> cartService.finalizeCart(cart.getCartID(), authenticationDataDto));
        }

        @Test
        void finalizeAccessDeniedException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .build();
            cartRepository.save(cart);
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", "something".toCharArray());

            //When & Then
            assertThrows(AccessDeniedException.class, () -> cartService.finalizeCart(cart.getCartID(), authenticationDataDto));
        }

        @Test
        void finalizeCartCartNotFoundException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
                try {
                    customerService.createNewCustomer(loggedCustomerDto);
                } catch (InvalidCustomerDataException e) {
                    e.printStackTrace();
                }
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(List.of())
                    .build();
            cartRepository.save(cart);
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());

            //When & Then
            assertThrows(CartNotFoundException.class, () -> cartService.finalizeCart(Long.MIN_VALUE, authenticationDataDto));
            assertThrows(CartNotFoundException.class, () -> cartService.finalizeCart(cart.getCartID(), authenticationDataDto));
        }

        @Test
        void finalizeCartPositive() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
                try {
                    customerService.createNewCustomer(loggedCustomerDto);
                } catch (InvalidCustomerDataException e) {
                    e.printStackTrace();
                }
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());

            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Item item = Item.builder()
                    .product(product)
                    .quantity(5)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(list)
                    .build();
            cartRepository.save(cart);

            //When & Then
            try {
                OrderDtoToCustomer orderDtoToCustomer = cartService.finalizeCart(cart.getCartID(), authenticationDataDto);
                assertEquals("UNPAID", orderDtoToCustomer.getStatus());
                assertEquals("25.00", orderDtoToCustomer.getTotalPrice());
                assertEquals("FINALIZED", cart.getCartStatus().toString());
            } catch (CartNotFoundException | UserNotFoundException | AccessDeniedException |
                     InvalidCustomerDataException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test payForCart")
    class TestPayForCart {
        @Test
        void payForCartUserNotFoundException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Optional<LoggedCustomer> customer_logged = customerRepository.findCustomer_LoggedByUserNameEquals("user");
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(List.of())
                    .build();
            cartRepository.save(cart);
            Order order = new Order(customer_logged.get(), cart, LocalDate.of(2023, 2, 25), ORDER_STATUS.UNPAID, "dummyList", new
                    BigDecimal("25.00"));
            orderRepository.save(order);
            System.out.println(order.getOrderID());
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("anotherUser", pwwd.toCharArray());

            //When & Then
            assertThrows(UserNotFoundException.class, () -> cartService.payForCart(order.getOrderID(), authenticationDataDto));
        }

        @Test
        void payForCartAccessDeniedException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Optional<LoggedCustomer> customer_logged = customerRepository.findCustomer_LoggedByUserNameEquals("user");
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(List.of())
                    .build();
            cartRepository.save(cart);
            Order order = new Order(customer_logged.get(), cart, LocalDate.of(2023, 2, 25), ORDER_STATUS.UNPAID, "dummyList", new
                    BigDecimal("25.00"));
            orderRepository.save(order);
            System.out.println(order.getOrderID());
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", "something".toCharArray());

            //When & Then
            assertThrows(AccessDeniedException.class, () -> cartService.payForCart(order.getOrderID(), authenticationDataDto));
        }

        @Test
        void payForCartOrderNotFoundException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Optional<LoggedCustomer> customer_logged = customerRepository.findCustomer_LoggedByUserNameEquals("user");
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(List.of())
                    .build();
            cartRepository.save(cart);
            Order order = new Order(customer_logged.get(), cart, LocalDate.of(2023, 2, 25), ORDER_STATUS.PAID, "dummyList", new
                    BigDecimal("25.00"));
            orderRepository.save(order);
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());

            //When & Then
            assertThrows(OrderNotFoundException.class, () -> cartService.payForCart(order.getOrderID() + 1, authenticationDataDto));
            assertThrows(OrderNotFoundException.class, () -> cartService.payForCart(order.getOrderID(), authenticationDataDto));
        }

        @Test
        void payForCartPositive() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Optional<LoggedCustomer> customer_logged = customerRepository.findCustomer_LoggedByUserNameEquals("user");

            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Item item = Item.builder()
                    .product(product)
                    .quantity(10)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(list)
                    .calculatedPrice(new BigDecimal("50.00"))
                    .build();
            cartRepository.save(cart);
            Order order = new Order(customer_logged.get(), cart, LocalDate.of(2023, 2, 25), ORDER_STATUS.UNPAID, "dummy list", cart.getCalculatedPrice());
            orderRepository.save(order);
            AuthenticationDataDto authenticationDataDto = new AuthenticationDataDto("user", pwwd.toCharArray());

            //When $ Then
            try {
                OrderDtoToCustomer orderDtoToCustomer = cartService.payForCart(order.getOrderID(), authenticationDataDto);
                assertNull(order.getCart());
                assertEquals("PAID", order.getOrder_status().toString());
                assertEquals("50.00", orderDtoToCustomer.getTotalPrice());
            } catch (UserNotFoundException | AccessDeniedException | OrderNotFoundException | PaymentErrorException |
                     InvalidCustomerDataException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test payForCartUnauthenticatedCustomer")
    class TestPayForCartUnauthenticatedCustomer {
        @Test
        void payForCartUnauthenticatedCustomerCartNotFoundException() {
            // Given
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(List.of())
                    .build();
            cartRepository.save(cart);
            UnauthenticatedCustomerDto unauthenticatedCustomerDto = new UnauthenticatedCustomerDto("Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");

            //When & Then
            assertThrows(CartNotFoundException.class, () -> cartService.payForCartUnauthenticatedCustomer(cart.getCartID() + 1, unauthenticatedCustomerDto));
            assertThrows(CartNotFoundException.class, () -> cartService.payForCartUnauthenticatedCustomer(cart.getCartID(), unauthenticatedCustomerDto));
        }

        @Test
        void payForCartUnauthenticatedCustomerInvalidCustomerDataException() {
            // Given
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(List.of())
                    .build();
            cartRepository.save(cart);
            UnauthenticatedCustomerDto unauthenticatedCustomerDto = new UnauthenticatedCustomerDto("", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            UnauthenticatedCustomerDto unauthenticatedCustomerDto1 = new UnauthenticatedCustomerDto(null, "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            UnauthenticatedCustomerDto unauthenticatedCustomerDto2 = new UnauthenticatedCustomerDto("Adam", "DDD", "ptrptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            UnauthenticatedCustomerDto unauthenticatedCustomerDto3 = new UnauthenticatedCustomerDto("Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-5558", "Maputo", "Mozambique");


            //When & Then
            assertAll(
                    () -> assertThrows(InvalidCustomerDataException.class, () -> cartService.payForCartUnauthenticatedCustomer(cart.getCartID(), unauthenticatedCustomerDto)),
                    () -> assertThrows(InvalidCustomerDataException.class, () -> cartService.payForCartUnauthenticatedCustomer(cart.getCartID(), unauthenticatedCustomerDto1)),
                    () -> assertThrows(InvalidCustomerDataException.class, () -> cartService.payForCartUnauthenticatedCustomer(cart.getCartID(), unauthenticatedCustomerDto2)),
                    () -> assertThrows(InvalidCustomerDataException.class, () -> cartService.payForCartUnauthenticatedCustomer(cart.getCartID(), unauthenticatedCustomerDto3)));
        }

        @Test
        void payForCartUnauthenticatedCustomerPositive() {
            // Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(new ArrayList<>())
                    .build();
            cartRepository.save(cart);
            Item item = Item.builder()
                    .product(product)
                    .quantity(5)
                    .cart(cart)
                    .build();
            itemRepository.save(item);
            cart.getListOfItems().add(item);
            cartRepository.save(cart);
            UnauthenticatedCustomerDto unauthenticatedCustomerDto = new UnauthenticatedCustomerDto("Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");

            //When & Then
            try {
                OrderDtoToCustomer orderDtoToCustomer = cartService.payForCartUnauthenticatedCustomer(cart.getCartID(), unauthenticatedCustomerDto);
                assertEquals("25.00", orderDtoToCustomer.getTotalPrice());
            } catch (CartNotFoundException | PaymentErrorException | InvalidCustomerDataException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test cancelOrder")
    class TestCancelOrder {
        @Test
        void cancelOrderOrderNotFoundException() {
            // Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Warehouse warehouse = new Warehouse(product, 20);
            warehouseRepository.save(warehouse);
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
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Optional<LoggedCustomer> customer_logged = customerRepository.findCustomer_LoggedByUserNameEquals("user");
            cartRepository.save(cart);
            Order order = new Order(customer_logged.get(), cart, LocalDate.of(2023, 2, 25), ORDER_STATUS.PAID, "dummyList", new
                    BigDecimal("25.00"));
            orderRepository.save(order);

            //When & Then
            assertThrows(OrderNotFoundException.class, () -> cartService.cancelOrder(order.getOrderID() + 1));
            assertThrows(OrderNotFoundException.class, () -> cartService.cancelOrder(order.getOrderID()));
        }

        @Test
        void cancelOrderPositive() {
            // Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Warehouse warehouse = new Warehouse(product, 20);
            warehouseRepository.save(warehouse);
            Item item = Item.builder()
                    .product(product)
                    .quantity(10)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            String pwwd = "password";
            LoggedCustomerDto loggedCustomerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(loggedCustomerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Optional<LoggedCustomer> customer_logged = customerRepository.findCustomer_LoggedByUserNameEquals("user");
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.FINALIZED)
                    .listOfItems(list)
                    .build();
            cartRepository.save(cart);
            Order order = new Order(customer_logged.get(), cart, LocalDate.of(2023, 2, 25), ORDER_STATUS.UNPAID, "dummyList", new
                    BigDecimal("25.00"));
            orderRepository.save(order);

            //When $ Then
            try {
                cartService.cancelOrder(order.getOrderID());
                assertFalse(orderRepository.existsById(order.getOrderID()));
                assertEquals(30, warehouse.getProductQuantity());
            } catch (OrderNotFoundException | ProductNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test cancelOrderLogged")
    class TestCancelOrderLogged {
        @Test
        void cancelOrderLoggedUserNotFoundException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto customerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(customerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Cart cart = new Cart(CartStatus.FINALIZED, List.of(), BigDecimal.TEN, LocalDate.of(2023, 2, 23));
            cartRepository.save(cart);
            Optional<LoggedCustomer> customer_logged = customerRepository.findCustomer_LoggedByUserNameEquals("user");
            AuthenticationDataDto loggedCustomerDto = new AuthenticationDataDto("anotherUser", pwwd.toCharArray());
            Order order = new Order(customer_logged.get(), cart, LocalDate.of(2023, 2, 25), ORDER_STATUS.UNPAID, "dummyList", new
                    BigDecimal("25.00"));

            orderRepository.save(order);

            //When & Then
            assertThrows(UserNotFoundException.class, () -> cartService.cancelOrderLogged(cart.getCartID(), loggedCustomerDto));
        }

        @Test
        void cancelOrderLoggedAccessDeniedException() {
            // Given
            String pwwd = "password";
            LoggedCustomerDto customerDto = new LoggedCustomerDto("user", pwwd, "Adam", "DDD", "ptr@ptr",
                    "Fairy", "5", "5", "55-555", "Maputo", "Mozambique");
            try {
                customerService.createNewCustomer(customerDto);
            } catch (InvalidCustomerDataException e) {
                e.printStackTrace();
            }
            Cart cart = new Cart(CartStatus.FINALIZED, List.of(), BigDecimal.TEN, LocalDate.of(2023, 2, 23));
            cartRepository.save(cart);
            AuthenticationDataDto loggedCustomerDto = new AuthenticationDataDto("user", "something".toCharArray());

            //When & Then
            assertThrows(AccessDeniedException.class, () -> cartService.cancelOrderLogged(cart.getCartID(), loggedCustomerDto));
        }
    }

    @Nested
    @Transactional
    @DisplayName("test deleteByCartStatus")
    class TestDeleteByCartStatus {
        @Test
        void deleteByCartStatusPositive() {
            //Given
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.EMPTY)
                    .build();
            Cart cart1 = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .build();
            cartRepository.save(cart);
            cartRepository.save(cart1);

            //When
            cartService.deleteByCartStatus(CartStatus.EMPTY);
            List<Cart> carts = cartRepository.findAll();

            //Then
            assertEquals(1, carts.size());
            assertFalse(carts.contains(cart));
        }
    }

   /* @Nested
    @Transactional
    @DisplayName("test deleteByProcessingTime")
    class TestDeleteByProcessingTime {
        @Test
        void deleteByProcessingTime() {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testName", "testDescription", category, new BigDecimal("5.00"));
            category.getListOfProducts().add(product);
            categoryRepository.save(category);
            productRepository.save(product);
            Warehouse warehouse = new Warehouse(product, 20);
            warehouseRepository.save(warehouse);
            Item item = Item.builder()
                    .product(product)
                    .quantity(8)
                    .build();
            List<Item> list = new ArrayList<>();
            list.add(item);
            Cart cart = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(list)
                    .created(LocalDate.now().minusDays(4))
                    .build();
            cartRepository.save(cart);
            Cart cart1 = Cart.builder()
                    .cartStatus(CartStatus.PROCESSING)
                    .listOfItems(list)
                    .created(LocalDate.now())
                    .build();
            cartRepository.save(cart1);

            try {
                cartService.deleteByProcessingTime();
                List<Cart> carts = cartRepository.findAll();
                assertEquals(1, carts.size());
                assertEquals(25, warehouse.getProductQuantity());
            } catch (CartNotFoundException e) {
                e.printStackTrace();
            }
        }
    }*/
}

