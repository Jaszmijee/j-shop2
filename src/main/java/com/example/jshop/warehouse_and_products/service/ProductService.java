package com.example.jshop.warehouse_and_products.service;

import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.error_handlers.exceptions.ProductNotFoundException;
import com.example.jshop.warehouse_and_products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product findProductById(Long productId) throws ProductNotFoundException {
        return productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }

    public void deleteProductById(Long productId) {
        productRepository.deleteByProductID(productId);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }
}
