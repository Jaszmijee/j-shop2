package com.example.jshop.warehouse_and_products.service;

import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.error_handlers.exceptions.CategoryNotFoundException;
import com.example.jshop.warehouse_and_products.domain.warehouse.Warehouse;
import com.example.jshop.error_handlers.exceptions.LimitException;
import com.example.jshop.warehouse_and_products.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public List<Warehouse> findAllProductsInWarehouse() {
        return warehouseRepository.findAll();
    }

    public Warehouse findWarehouseByProductId(Long itemId) {
        return warehouseRepository.findWarehouseByProductId(itemId).orElse(null);
    }

    public List<Warehouse> findProductsInWarehouseByCategory(String category, Integer limit) throws CategoryNotFoundException {
        List<Warehouse> list = warehouseRepository.findWarehouseByProduct_Category_Name(category, limit);
        if (list.isEmpty()) {
            throw new CategoryNotFoundException();
        }
        return list;
    }

    public Warehouse save(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public void deleteProductFromWarehouseByProductId(Long productId) {
        warehouseRepository.deleteByProduct_ProductID(productId);
    }

    public List<Warehouse> findProductsInWarehouseWithSelection(String categoryName, String productName, BigDecimal productPrice, Integer limit) throws LimitException {
        if (limit > 100 || limit < 1) {
            throw new LimitException();
        }
        return warehouseRepository.findWarehouseByProduct_CategoryOrProduct_ProductNameOAndProduct_Price(categoryName, productName, productPrice, limit);
    }

    public void sentForShipment(Order createdOrder) {
        String shipment = "\n" + createdOrder.getListOfProducts() + "\n" + createdOrder.getLoggedCustomer().getAddress();
        System.out.println("prepare and send shipment: " + shipment);
    }
}
