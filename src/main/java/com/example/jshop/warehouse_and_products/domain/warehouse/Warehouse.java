package com.example.jshop.warehouse_and_products.domain.warehouse;

import com.example.jshop.warehouse_and_products.domain.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "warehouse")
public class Warehouse {

    @Id
    @GeneratedValue
    private Long warehouseID;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "products_product_id")
    private Product product;

    @Column(name = "quantity")
    private Integer productQuantity;

    public Warehouse(Product product, Integer productQuantity) {
        this.product = product;
        this.productQuantity = productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
    }
}
