package com.example.jshop.warehouse_and_products.repository;

import com.example.jshop.warehouse_and_products.domain.category.Category;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface CategoryRepository extends CrudRepository<Category, Long> {

    @Override
    List<Category> findAll();

    Category findByNameEqualsIgnoreCase(String name);

    @Override
    Category save(Category category);

    void deleteByNameEqualsIgnoreCase(String name);
}
