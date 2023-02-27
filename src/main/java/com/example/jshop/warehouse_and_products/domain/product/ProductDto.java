package com.example.jshop.warehouse_and_products.domain.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductDto {

    private String productName;
    private String description;
    private String categoryName;
    private BigDecimal price;

}
