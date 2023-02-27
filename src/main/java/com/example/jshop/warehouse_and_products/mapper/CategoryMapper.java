package com.example.jshop.warehouse_and_products.mapper;

import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.category.CategoryDto;
import com.example.jshop.warehouse_and_products.domain.category.CategoryWithProductsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class CategoryMapper {

    @Autowired
    ProductMapper productMapper;

    public Category mapToCategory(CategoryDto categoryDto) {
        return new Category(categoryDto.getName());
    }

    public List<CategoryWithProductsDto> mapToCategoryDtoListAllInfo(List<Category> categoryList) {
        return categoryList.stream()
                .map(category -> new CategoryWithProductsDto(category.getCategoryID(), category.getName(),
                        productMapper.mapToProductDtoList(category.getListOfProducts()))).collect(toList());
    }

    public CategoryWithProductsDto mapToCategoryDtoAllInfo(Category category) {
        return new CategoryWithProductsDto(category.getCategoryID(), category.getName(),
                productMapper.mapToProductDtoList(category.getListOfProducts()));
    }
}
