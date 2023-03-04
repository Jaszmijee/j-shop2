package com.example.jshop.carts_and_orders.domain.order;

import com.example.jshop.carts_and_orders.domain.cart.Cart;
import com.example.jshop.customer.domain.LoggedCustomer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private Long orderID;

    @ManyToOne
    @JoinColumn(name = "logged_customers_customerID")
    private LoggedCustomer loggedCustomer;

    @Column(name = "created")
    private LocalDate created;

    @Column(name = "paid")
    private LocalDate paid;

    @Enumerated(value = EnumType.STRING)
    private ORDER_STATUS order_status;

    @Column(name = "items", columnDefinition = "TEXT")
    private String listOfProducts;

    @Column(name = "calculatedValue")
    private BigDecimal calculatedPrice;

    @Column(name = "paymentDue")
    private LocalDate paymentDue;

    @OneToOne
    @JoinColumn(name = "carts_cart_id")
    private Cart cart;

    public Order(LoggedCustomer loggedCustomer, Cart cart, LocalDate created, ORDER_STATUS order_status, String listOfProducts, BigDecimal calculatedPrice) {
        this.loggedCustomer = loggedCustomer;
        this.created = created;
        this.order_status = order_status;
        this.listOfProducts = listOfProducts;
        this.calculatedPrice = calculatedPrice;
        this.cart = cart;
    }

   public void setPaid(LocalDate paid) {
        this.paid = paid;
    }

    public void setOrder_status(ORDER_STATUS order_status) {
        this.order_status = order_status;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setLoggedCustomer(LoggedCustomer loggedCustomer) {
        this.loggedCustomer = loggedCustomer;
    }

    public void setPaymentDue(LocalDate paymentDue) {
        this.paymentDue = paymentDue;
    }
}

