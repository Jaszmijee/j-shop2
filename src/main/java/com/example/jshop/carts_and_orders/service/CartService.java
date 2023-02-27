package com.example.jshop.carts_and_orders.service;

import com.example.jshop.carts_and_orders.domain.cart.*;
import com.example.jshop.customer.domain.LoggedCustomer;
import com.example.jshop.customer.service.CustomerService;
import com.example.jshop.carts_and_orders.domain.order.ORDER_STATUS;
import com.example.jshop.customer.domain.Address;
import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.customer.domain.AuthenticationDataDto;
import com.example.jshop.customer.domain.UnauthenticatedCustomerDto;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.email.service.EmailContentCreator;
import com.example.jshop.error_handlers.exceptions.*;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.domain.warehouse.Warehouse;
import com.example.jshop.email.service.SimpleEmailService;
import com.example.jshop.carts_and_orders.mapper.CartMapper;
import com.example.jshop.carts_and_orders.mapper.ItemMapper;
import com.example.jshop.carts_and_orders.mapper.OrderMapper;
import com.example.jshop.carts_and_orders.repository.CartRepository;
import com.example.jshop.warehouse_and_products.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final WarehouseService warehouseService;
    private final CartMapper cartMapper;
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CustomerService customerService;
    private final SimpleEmailService emailService;
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final EmailContentCreator emailCreator;

    private Cart findCartById(Long cartId) throws CartNotFoundException {
        return cartRepository.findById(cartId).orElseThrow(CartNotFoundException::new);
    }

    public Cart createCart() {
        Cart newCart = new Cart();
        newCart.setCartStatus(CartStatus.EMPTY);
        newCart.setCreated(LocalDate.now());
        cartRepository.save(newCart);
        return newCart;
    }

    private void updateProductInWarehouse(Warehouse warehouse, Integer productQuantity) {
        warehouse.setProductQuantity(warehouse.getProductQuantity() - productQuantity);
        warehouseService.save(warehouse);
    }

    private BigDecimal calculateCurrentCartValue(Cart cart) {
        if (cart.getCartStatus() == CartStatus.PROCESSING) {
            return new BigDecimal(String.valueOf(cart.getListOfItems().stream()
                    .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, (sum, item) -> sum = sum.add(item))));
        } else return BigDecimal.ZERO;
    }

    private void validateCartForProcessing(Cart cart) throws CartNotFoundException {
        if (cart.getCartStatus() == CartStatus.FINALIZED) {
            throw new CartNotFoundException();
        }
    }

    private Warehouse validateProductInWarehouse(Long productId, int quantity) throws ProductNotFoundException, NotEnoughItemsException {
        Warehouse warehouse = warehouseService.findWarehouseByProductId(productId);
        if (warehouse == null || warehouse.getProductQuantity() == 0) {
            throw new ProductNotFoundException();
        } else if (warehouse.getProductQuantity() < quantity) {
            throw new NotEnoughItemsException();
        } else return warehouse;
    }

    public CartDto addToCart(Long cartId, CartItemsDto cartItemsDto) throws CartNotFoundException, NotEnoughItemsException, ProductNotFoundException, InvalidQuantityException {
        validateQuantityOfPurchasedProduct(cartItemsDto.getQuantity());
        Cart cartToUpdate = findCartById(cartId);
        validateCartForProcessing(cartToUpdate);
        Warehouse warehouse = validateProductInWarehouse(cartItemsDto.getProductId(), cartItemsDto.getQuantity());
        Product product = warehouse.getProduct();
        Item item;
        List<Item> items = cartToUpdate.getListOfItems().stream().filter(i -> i.getProduct().getProductID().equals(product.getProductID()))
                .toList();
        if (items.size() > 0) {
            item = items.get(0);
            item.setQuantity(item.getQuantity() + cartItemsDto.getQuantity());
        } else {
            item = Item.builder()
                    .product(product)
                    .quantity(cartItemsDto.getQuantity())
                    .cart(cartToUpdate)
                    .build();
            cartToUpdate.getListOfItems().add(item);
        }
        itemService.save(item);
        cartToUpdate.setCartStatus(CartStatus.PROCESSING);
        cartToUpdate.setCalculatedPrice(calculateCurrentCartValue(cartToUpdate));
        updateProductInWarehouse(warehouse, cartItemsDto.getQuantity());
        cartRepository.save(cartToUpdate);
        itemService.save(item);
        return cartMapper.mapCartToCartDto(cartToUpdate);
    }

    private void validateQuantityOfPurchasedProduct(int quantity) throws InvalidQuantityException {
        if (quantity <= 0) {
            throw new InvalidQuantityException();
        }
    }

    public Cart showCart(Long cartId) throws CartNotFoundException {
        return cartRepository.findById(cartId).orElseThrow(CartNotFoundException::new);
    }

    public CartDto removeFromCart(Long cartId, CartItemsDto cartItemsDto) throws CartNotFoundException, ProductNotFoundException, InvalidQuantityException {
        validateQuantityOfPurchasedProduct(cartItemsDto.getQuantity());
        Cart cartToUpdate = findCartById(cartId);
        validateCartForProcessing(cartToUpdate);
        if (cartToUpdate.getCartStatus() == CartStatus.EMPTY) {
            throw new CartNotFoundException();
        }
        Item item;
        List<Item> items = cartToUpdate.getListOfItems().stream().filter(i -> i.getProduct().getProductID().equals(cartItemsDto.getProductId()))
                .toList();
        if (items.size() == 0) {
            throw new ProductNotFoundException();
        }
        Warehouse warehouse = warehouseService.findWarehouseByProductId(cartItemsDto.getProductId());
        item = items.get(0);
        if (item.getQuantity() <= cartItemsDto.getQuantity()) {
            updateProductInWarehouse(warehouse, -(item.getQuantity()));
            cartToUpdate.getListOfItems().remove(item);
            itemService.delete(item);
        } else {
            updateProductInWarehouse(warehouse, -(cartItemsDto.getQuantity()));
            item.setQuantity(item.getQuantity() - cartItemsDto.getQuantity());
            itemService.save(item);
            cartToUpdate.getListOfItems().set(cartToUpdate.getListOfItems().indexOf(item), item);
        }
        if (cartToUpdate.getListOfItems().isEmpty()) {
            cartToUpdate.setCartStatus(CartStatus.EMPTY);
        }
        cartToUpdate.setCalculatedPrice(calculateCurrentCartValue(cartToUpdate));
        cartRepository.save(cartToUpdate);
        return cartMapper.mapCartToCartDto(cartToUpdate);
    }

    public void cancelCart(Long cartId) throws CartNotFoundException {
        Cart cart = findCartById(cartId);
        validateCartForProcessing(cart);
        if (!(cart.getListOfItems().isEmpty())) {
            for (Item items : cart.getListOfItems()) {
                Warehouse warehouse = warehouseService.findWarehouseByProductId(items.getProduct().getProductID());
                warehouse.setProductQuantity(warehouse.getProductQuantity() + items.getQuantity());
                warehouseService.save(warehouse);
                itemService.delete(items);
            }
        }
        cartRepository.deleteById(cartId);
    }

    private Order createNewOrder(Long cartId, AuthenticationDataDto authenticationDataDto) throws CartNotFoundException, UserNotFoundException, AccessDeniedException {
        Cart cart = findCartById(cartId);
        LoggedCustomer loggedCustomer = customerService.verifyLogin(authenticationDataDto.getUsername(), authenticationDataDto.getPassword());
        String listOfItems = cart.getListOfItems().stream()
                .map(itemMapper::mapToItemDto)
                .map(result -> "\nproduct: " + result.getProductName() + ", quantity: " + result.getProductQuantity() + ", total price: " + result.getCalculatedPrice())
                .collect(Collectors.joining("\n"));
        cart.setCalculatedPrice(calculateCurrentCartValue(cart));
        BigDecimal calculatedPrice = calculateCurrentCartValue(cart);
        Order createdOrder = new Order(loggedCustomer, cart, LocalDate.now(), ORDER_STATUS.UNPAID, listOfItems, calculatedPrice);
        loggedCustomer.getListOfOrders().add(createdOrder);
        customerService.updateCustomer(loggedCustomer);
        orderService.save(createdOrder);
        return createdOrder;
    }

    public OrderDtoToCustomer finalizeCart(Long cartId, AuthenticationDataDto authenticationDataDto) throws CartNotFoundException, UserNotFoundException, AccessDeniedException {
        customerService.verifyLogin(authenticationDataDto.getUsername(), authenticationDataDto.getPassword());
        Order order = createNewOrder(cartId, authenticationDataDto);
        emailService.send(emailCreator.createContent(order));
        Cart cart = findCartById(cartId);
        validateCartForProcessing(cart);
        cart.setCartStatus(CartStatus.FINALIZED);
        cartRepository.save(cart);
        return orderMapper.mapToOrderDtoToCustomer(order);
    }

    private Order validateOrderToPay(Long orderId, String username) throws OrderNotFoundException {
        Order order = orderService.findByIdAndUserName(orderId, username);
        if (order.getOrder_status() == ORDER_STATUS.PAID) {
            throw new OrderNotFoundException();
        } else return order;
    }

    //TODO transaction with Bank
    private boolean orderIsPaid(Order order) {
        return true;
    }

    public OrderDtoToCustomer payForCart(Long orderId, AuthenticationDataDto authenticationDataDto) throws UserNotFoundException, AccessDeniedException, OrderNotFoundException, PaymentErrorException {
        customerService.verifyLogin(authenticationDataDto.getUsername(), authenticationDataDto.getPassword());
        Order order = validateOrderToPay(orderId, authenticationDataDto.getUsername());
        boolean isPaid = orderIsPaid(order);
        if (!(isPaid)) {
            throw new PaymentErrorException();
        } else {
            emailService.send(emailCreator.createContent(order));
            Cart cart = order.getCart();
            order.setCart(null);
            order.setOrder_status(ORDER_STATUS.PAID);
            order.setPaid(LocalDate.now());
            orderService.save(order);
            cartRepository.deleteById(cart.getCartID());
            warehouseService.sentForShipment(order);
        }
        return orderMapper.mapToOrderDtoToCustomer(order);
    }

    public OrderDtoToCustomer payForCartUnauthenticatedCustomer(Long cartId, UnauthenticatedCustomerDto unauthenticatedCustomerDto) throws CartNotFoundException, PaymentErrorException, InvalidCustomerDataException {
        checkCustomerDataValidity(unauthenticatedCustomerDto);
        Cart cart = findCartById(cartId);
        validateCartForProcessing(cart);
        cart.setCalculatedPrice(calculateCurrentCartValue(cart));
        cartRepository.save(cart);
        String listOfItems = cart.getListOfItems().stream()
                .map(itemMapper::mapToItemDto)
                .map(result -> "product: " + result.getProductName() + ", quantity: " + result.getProductQuantity() + ", total price: " + result.getCalculatedPrice())
                .collect(Collectors.joining("\n"));
        Order createdOrder = new Order(new LoggedCustomer(null, null, unauthenticatedCustomerDto.getFirstName(), unauthenticatedCustomerDto.getLastName(),
                unauthenticatedCustomerDto.getEmail(), new Address(unauthenticatedCustomerDto.getStreet(), unauthenticatedCustomerDto.getHouseNo(), unauthenticatedCustomerDto.getFlatNo(), unauthenticatedCustomerDto.getZipCode(), unauthenticatedCustomerDto.getCity(), unauthenticatedCustomerDto.getCountry())),
                cart, LocalDate.now(), ORDER_STATUS.UNPAID, listOfItems, cart.getCalculatedPrice());
        customerService.updateCustomer(createdOrder.getLoggedCustomer());
        orderService.save(createdOrder);
        boolean isPaid = orderIsPaid(createdOrder);
        if (!(isPaid)) {
            throw new PaymentErrorException();
        } else {
            warehouseService.sentForShipment(createdOrder);
            Cart cartToPay = createdOrder.getCart();
            emailService.send(emailCreator.createContent(createdOrder));
            createdOrder.setCart(null);
            createdOrder.setOrder_status(ORDER_STATUS.PAID);
            createdOrder.setPaid(LocalDate.now());
            orderService.save(createdOrder);

            long customerId = createdOrder.getLoggedCustomer().getCustomerID();
            createdOrder.setLoggedCustomer(null);
            orderService.save(createdOrder);
            customerService.deleteUnauthenticatedCustomer(customerId);
            cartRepository.deleteById(cartToPay.getCartID());
        }
        return orderMapper.mapToOrderDtoToCustomer(createdOrder);
    }

    private void checkCustomerDataValidity(UnauthenticatedCustomerDto unauthenticatedCustomerDto) throws InvalidCustomerDataException {
        if ((unauthenticatedCustomerDto.getFirstName().isEmpty())
                || (unauthenticatedCustomerDto.getLastName().isEmpty())
                || (unauthenticatedCustomerDto.getStreet().isEmpty())
                || (unauthenticatedCustomerDto.getHouseNo().isEmpty())
                || (unauthenticatedCustomerDto.getFlatNo().isEmpty())
                || (unauthenticatedCustomerDto.getCity().isEmpty())
                || (unauthenticatedCustomerDto.getZipCode().isEmpty())
                || (unauthenticatedCustomerDto.getEmail().isEmpty())) {
            throw new InvalidCustomerDataException();
        }
    }

    public void cancelOrder(Long orderId) throws OrderNotFoundException {
        Order orderToCancel = orderService.findOrderById(orderId);
        if (orderToCancel.getOrder_status() == ORDER_STATUS.PAID) {
            throw new OrderNotFoundException();
        }
        for (Item items : orderToCancel.getCart().getListOfItems()) {
            Warehouse warehouse = warehouseService.findWarehouseByProductId(items.getProduct().getProductID());
            warehouse.setProductQuantity(warehouse.getProductQuantity() + items.getQuantity());
            warehouseService.save(warehouse);
            items.setCart(null);
        }
        orderToCancel.getCart().getListOfItems().clear();
        orderService.save(orderToCancel);
        emailService.send(emailCreator.createContent(orderToCancel));
        LoggedCustomer _Logged_Customer = orderToCancel.getLoggedCustomer();
        _Logged_Customer.getListOfOrders().remove((orderToCancel));
        customerService.updateCustomer(_Logged_Customer);
        orderToCancel.setLoggedCustomer(null);
        Long cartId = orderToCancel.getCart().getCartID();
        orderToCancel.setCart(null);
        cartRepository.deleteById(cartId);
        orderService.deleteOrder(orderToCancel);
    }

    public void cancelOrderLogged(Long orderId, AuthenticationDataDto authenticationDataDto) throws UserNotFoundException, AccessDeniedException, OrderNotFoundException {
        customerService.verifyLogin(authenticationDataDto.getUsername(), authenticationDataDto.getPassword());
        orderService.findByIdAndUserName(orderId, authenticationDataDto.getUsername());
        cancelOrder(orderId);
    }

    public void deleteByCartStatus(CartStatus cartStatus) {
        cartRepository.deleteByCartStatus(cartStatus);
    }

    public void deleteByProcessingTime() throws CartNotFoundException {
        List<Cart> listOfCarts = cartRepository.selectByProcessingTime();
        for (Cart cart : listOfCarts) {
            cancelCart(cart.getCartID());
        }
    }
}






