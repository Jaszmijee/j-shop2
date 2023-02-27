package com.example.jshop.warehouse_and_products.mapper;

import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.domain.product.ProductDto;
import com.example.jshop.warehouse_and_products.domain.product.ProductDtoAllInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductMapper {

    public Product mapToProduct(ProductDto productDto) {
        return new Product(

                productDto.getProductName(),
                productDto.getDescription(),
                new Category(productDto.getCategoryName()),
                productDto.getPrice()
        );
    }

    public ProductDtoAllInfo mapToProductDtoAllInfo(Product product) {
        return new ProductDtoAllInfo(

                product.getProductID(),
                product.getCategory().getName(),
                product.getProductName(),
                product.getDescription(),
                product.getPrice()
        );
    }

    public List<ProductDtoAllInfo> mapToProductDtoList(List<Product> productList) {
        List<ProductDtoAllInfo> productDtos = new ArrayList<>();
        for (Product product : productList) {
            productDtos.add(mapToProductDtoAllInfo(product));
        }
        return productDtos;
    }
}
