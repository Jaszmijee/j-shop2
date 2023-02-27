package com.example.jshop.carts_and_orders.domain.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private Long cartID;
    private List<ItemDto> listOfProducts;
    private CartStatus cartStatus;
    private BigDecimal calculatedPrice;
}
