package com.example.jshop.carts_and_orders.domain.cart;

import com.example.jshop.warehouse_and_products.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "items")
public class Item {

    @Id
    @GeneratedValue
    private Long itemId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "products_product_id")
    private Product product;

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "carts_cartID")
    private Cart cart;

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}

