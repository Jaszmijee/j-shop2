package com.example.jshop.carts_and_orders.service;

import com.example.jshop.carts_and_orders.domain.cart.Item;
import com.example.jshop.carts_and_orders.repository.ItemRepository;
import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.repository.CategoryRepository;
import com.example.jshop.warehouse_and_products.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemServiceTest {

    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @Nested
    @DisplayName("test save")
    @Transactional
    class TestSave {
        @Test
        void savePositive() {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("testProduct", "testDescription", category, BigDecimal.TEN);
            productRepository.save(product);
            Item item = Item.builder()
                    .product(product)
                    .quantity(20)
                    .build();

            //When
            Item savedItem = itemRepository.save(item);
            Long itemId = savedItem.getItemId();

            //Then
            assertTrue(itemRepository.existsById(itemId));
            assertEquals(20, itemRepository.findById(itemId).get().getQuantity());
        }
    }
    @Nested
    @Transactional
    @DisplayName("test delete")
    class TestDelete {
    @Test
    void deletePositive() {
        //Given
        Category category = new Category("testCategory");
        categoryRepository.save(category);
        Product product = new Product("testProduct", "testDescription", category, BigDecimal.TEN);
        productRepository.save(product);
        Item item = Item.builder()
                .product(product)
                .quantity(20)
                .build();
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getItemId();

        //When
       itemRepository.delete(item);

        //Then
        assertFalse(itemRepository.existsById(itemId));
    }
    }
}