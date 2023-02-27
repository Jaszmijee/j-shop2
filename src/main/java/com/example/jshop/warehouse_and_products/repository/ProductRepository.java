package com.example.jshop.warehouse_and_products.repository;

import com.example.jshop.warehouse_and_products.domain.product.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ProductRepository extends CrudRepository<Product, Long> {

    @Override
    Product save(Product product);

    Product findByProductName(String name);

    @Override
    Optional<Product> findById(Long productId);

    Optional<Product> findByProductID(Long productId);

    void deleteByProductID(Long productId);

    @Override
    List<Product> findAll();
}
