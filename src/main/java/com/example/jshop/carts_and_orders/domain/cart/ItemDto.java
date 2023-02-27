package com.example.jshop.carts_and_orders.domain.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ItemDto {

    private Long productId;
    private String productName;
    private int productQuantity;
    private BigDecimal calculatedPrice;
}
