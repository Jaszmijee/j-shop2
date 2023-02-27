package com.example.jshop.warehouse_and_products.domain.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductDtoAllInfo {

    private Long productId;
    private String category;
    private String productName;
    private String description;
    private BigDecimal price;
}
