package com.example.jshop.customer.domain;

import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.carts_and_orders.domain.cart.Cart;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "logged_customers")
public class LoggedCustomer {

    @Id
    @GeneratedValue
    private Long customerID;

    @Column(name = "userName", unique = true)
    private String userName;

    @Column(name = "password")
    private char[] password;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "carts_CartId")
    private Cart cart;

    @Column(name = "name")
    @NonNull
    private String firstName;

    @Column(name = "lastname")
    @NonNull
    private String lastName;

    @Column(name = "e-mail")
    @NonNull
    private String email;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_addresId")
    private Address address;

    @OneToMany(targetEntity = Order.class,
            mappedBy = "loggedCustomer",
            fetch = FetchType.LAZY)
    List<Order> listOfOrders = new ArrayList<>();

    public LoggedCustomer(String userName, char[] password, @NonNull String firstName, @NonNull String lastName, @NonNull String email, @NonNull Address address) {
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }
}

