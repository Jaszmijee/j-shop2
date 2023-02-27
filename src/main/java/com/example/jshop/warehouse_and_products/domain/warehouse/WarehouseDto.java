package com.example.jshop.warehouse_and_products.domain.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WarehouseDto {

    private Long warehouseId;
    private Long productId;
    private String productName;
    private String category;
    private BigDecimal price;
    private int quantity;


}
