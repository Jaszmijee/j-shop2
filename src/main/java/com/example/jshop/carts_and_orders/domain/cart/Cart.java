package com.example.jshop.carts_and_orders.domain.cart;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity(name = "carts")
public class Cart {

    @Id
    @GeneratedValue
    private Long cartID;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CartStatus cartStatus;

    @Column(name = "items_cart")
    @OneToMany(targetEntity = Item.class,
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private List<Item> listOfItems = new ArrayList<>();

    @Column(name = "total")
    private BigDecimal calculatedPrice;

    @Column(name = "created")
    private LocalDate created;

    public Cart(CartStatus cartStatus, List<Item> listOfItems, BigDecimal calculatedPrice, LocalDate created) {
        this.cartStatus = cartStatus;
        this.listOfItems = listOfItems;
        this.calculatedPrice = calculatedPrice;
        this.created = created;
    }

    public void setCartStatus(CartStatus cartStatus) {
        this.cartStatus = cartStatus;
    }

    public void setCalculatedPrice(BigDecimal calculatedPrice) {
        this.calculatedPrice = calculatedPrice;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }
}
